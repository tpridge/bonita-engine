/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import com.bonitasoft.engine.bdm.lazy.LazyLoaded;
import com.bonitasoft.engine.bdm.model.BusinessObject;
import com.bonitasoft.engine.bdm.model.Index;
import com.bonitasoft.engine.bdm.model.Query;
import com.bonitasoft.engine.bdm.model.UniqueConstraint;
import com.bonitasoft.engine.bdm.model.field.Field;
import com.bonitasoft.engine.bdm.model.field.FieldType;
import com.bonitasoft.engine.bdm.model.field.RelationField;
import com.bonitasoft.engine.bdm.model.field.SimpleField;
import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;

/**
 * @author Colin PUY
 */
public class EntityCodeGenerator {

    private final CodeGenerator codeGenerator;

    public EntityCodeGenerator(final CodeGenerator codeGenerator) {
        this.codeGenerator = codeGenerator;
    }

    public JDefinedClass addEntity(final BusinessObject bo) throws JClassAlreadyExistsException {
        final String qualifiedName = bo.getQualifiedName();
        validateClassNotExistsInRuntime(qualifiedName);

        JDefinedClass entityClass = codeGenerator.addClass(qualifiedName);
        entityClass = codeGenerator.addInterface(entityClass, com.bonitasoft.engine.bdm.Entity.class.getName());
        entityClass.javadoc().add(bo.getDescription());

        final JAnnotationUse entityAnnotation = codeGenerator.addAnnotation(entityClass, Entity.class);
        entityAnnotation.param("name", entityClass.name());

        addIndexAnnotations(bo, entityClass);
        addUniqueConstraintAnnotations(bo, entityClass);
        addQueriesAnnotation(bo, entityClass);

        addFieldsAndMethods(bo, entityClass);

        codeGenerator.addDefaultConstructor(entityClass);

        codeGenerator.addEqualsMethod(entityClass);
        codeGenerator.addHashCodeMethod(entityClass);

        return entityClass;
    }

    private void addFieldsAndMethods(final BusinessObject bo, final JDefinedClass entityClass) throws JClassAlreadyExistsException {
        addPersistenceIdFieldAndAccessors(entityClass);
        addPersistenceVersionFieldAndAccessors(entityClass);

        for (final Field field : bo.getFields()) {
            final JFieldVar fieldVar = addField(entityClass, field);
            addAccessors(entityClass, fieldVar, field);
            addModifiers(entityClass, field);
        }
    }

    private void addQueriesAnnotation(final BusinessObject bo, final JDefinedClass entityClass) {
        final JAnnotationUse namedQueriesAnnotation = codeGenerator.addAnnotation(entityClass, NamedQueries.class);
        final JAnnotationArrayMember valueArray = namedQueriesAnnotation.paramArray("value");

        // Add provided queries
        for (final Query providedQuery : BDMQueryUtil.createProvidedQueriesForBusinessObject(bo)) {
            addNamedQuery(entityClass, valueArray, providedQuery.getName(), providedQuery.getContent());
        }

        // Add custom queries
        for (final Query query : bo.getQueries()) {
            addNamedQuery(entityClass, valueArray, query.getName(), query.getContent());
        }
    }

    private void addUniqueConstraintAnnotations(final BusinessObject bo, final JDefinedClass entityClass) {
        final JAnnotationUse tableAnnotation = codeGenerator.addAnnotation(entityClass, Table.class);
        tableAnnotation.param("name", entityClass.name().toUpperCase());

        final List<UniqueConstraint> uniqueConstraints = bo.getUniqueConstraints();
        if (!uniqueConstraints.isEmpty()) {
            final JAnnotationArrayMember uniqueConstraintsArray = tableAnnotation.paramArray("uniqueConstraints");
            for (final UniqueConstraint uniqueConstraint : uniqueConstraints) {
                final JAnnotationUse uniqueConstraintAnnotatation = uniqueConstraintsArray.annotate(javax.persistence.UniqueConstraint.class);
                uniqueConstraintAnnotatation.param("name", uniqueConstraint.getName().toUpperCase());
                final JAnnotationArrayMember columnNamesParamArray = uniqueConstraintAnnotatation.paramArray("columnNames");
                for (final String fieldName : uniqueConstraint.getFieldNames()) {
                    columnNamesParamArray.param(fieldName.toUpperCase());
                }
            }
        }
    }

    private void addIndexAnnotations(final BusinessObject bo, final JDefinedClass entityClass) {
        final List<Index> indexes = bo.getIndexes();
        if (indexes != null && !indexes.isEmpty()) {
            final JAnnotationUse hibTabAnnotation = codeGenerator.addAnnotation(entityClass, org.hibernate.annotations.Table.class);
            hibTabAnnotation.param("appliesTo", entityClass.name().toUpperCase());
            final JAnnotationArrayMember indexesArray = hibTabAnnotation.paramArray("indexes");
            for (final Index index : indexes) {
                final JAnnotationUse indexAnnotation = indexesArray.annotate(org.hibernate.annotations.Index.class);
                indexAnnotation.param("name", index.getName().toUpperCase());
                final JAnnotationArrayMember columnParamArray = indexAnnotation.paramArray("columnNames");
                for (final String fieldName : index.getFieldNames()) {
                    columnParamArray.param(fieldName.toUpperCase());
                }
            }
        }
    }

    private void addNamedQuery(final JDefinedClass entityClass, final JAnnotationArrayMember valueArray, final String name, final String content) {
        final JAnnotationUse nameQueryAnnotation = valueArray.annotate(NamedQuery.class);
        nameQueryAnnotation.param("name", entityClass.name() + "." + name);
        nameQueryAnnotation.param("query", content);
    }

    private void validateClassNotExistsInRuntime(final String qualifiedName) {
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        boolean alreadyInRuntime = true;
        try {
            contextClassLoader.loadClass(qualifiedName);
        } catch (final ClassNotFoundException e) {
            alreadyInRuntime = false;
        }
        if (alreadyInRuntime) {
            throw new IllegalArgumentException("Class " + qualifiedName + " already exists in target runtime environment.");
        }
    }

    public void addPersistenceIdFieldAndAccessors(final JDefinedClass entityClass) throws JClassAlreadyExistsException {
        final JFieldVar idFieldVar = codeGenerator.addField(entityClass, Field.PERSISTENCE_ID, codeGenerator.toJavaClass(FieldType.LONG));
        codeGenerator.addAnnotation(idFieldVar, Id.class);
        codeGenerator.addAnnotation(idFieldVar, GeneratedValue.class);
        addAccessors(entityClass, idFieldVar);
    }

    public void addPersistenceVersionFieldAndAccessors(final JDefinedClass entityClass) throws JClassAlreadyExistsException {
        final JFieldVar versionField = codeGenerator.addField(entityClass, Field.PERSISTENCE_VERSION, codeGenerator.toJavaClass(FieldType.LONG));
        codeGenerator.addAnnotation(versionField, Version.class);
        addAccessors(entityClass, versionField);
    }

    public JFieldVar addField(final JDefinedClass entityClass, final Field field) throws JClassAlreadyExistsException {
        JFieldVar fieldVar = null;
        if (field.isCollection()) {
            fieldVar = codeGenerator.addListField(entityClass, field);
        } else {
            fieldVar = codeGenerator.addField(entityClass, field.getName(), codeGenerator.toJavaClass(field));
        }
        annotateField(entityClass, field, fieldVar);
        return fieldVar;
    }

    private void annotateField(final JDefinedClass entityClass, final Field field, final JFieldVar fieldVar) {
        if (field instanceof SimpleField) {
            annotateSimpleField((SimpleField) field, fieldVar);
        } else if (field instanceof RelationField) {
            annotateRelationField(entityClass, (RelationField) field, fieldVar);
        }
    }

    private void annotateRelationField(final JDefinedClass entityClass, final RelationField rfield, final JFieldVar fieldVar) {
        new RelationFieldAnnotator(codeGenerator).annotateRelationField(entityClass, rfield, fieldVar);
    }

    private void annotateSimpleField(final SimpleField sfield, final JFieldVar fieldVar) {
        if (sfield.isCollection()) {
            final JAnnotationUse collectionAnnotation = codeGenerator.addAnnotation(fieldVar, ElementCollection.class);
            collectionAnnotation.param("fetch", FetchType.EAGER);
            codeGenerator.addAnnotation(fieldVar, OrderColumn.class);
        }
        final JAnnotationUse columnAnnotation = codeGenerator.addAnnotation(fieldVar, Column.class);
        columnAnnotation.param("name", sfield.getName().toUpperCase());
        columnAnnotation.param("nullable", sfield.isNullable());

        if (sfield.getType() == FieldType.DATE) {
            final JAnnotationUse temporalAnnotation = codeGenerator.addAnnotation(fieldVar, Temporal.class);
            temporalAnnotation.param("value", TemporalType.TIMESTAMP);
        } else if (FieldType.TEXT == sfield.getType()) {
            codeGenerator.addAnnotation(fieldVar, Lob.class);
        } else if (FieldType.STRING == sfield.getType() && sfield.getLength() != null && sfield.getLength() > 0) {
            columnAnnotation.param("length", sfield.getLength());
        }
    }

    public void addAccessors(final JDefinedClass entityClass, final JFieldVar fieldVar) throws JClassAlreadyExistsException {
        addAccessors(entityClass, fieldVar, null);
    }

    public void addAccessors(final JDefinedClass entityClass, final JFieldVar fieldVar, final Field field) throws JClassAlreadyExistsException {
        codeGenerator.addSetter(entityClass, fieldVar);
        final JMethod getter = codeGenerator.addGetter(entityClass, fieldVar);
        if (field instanceof RelationField && ((RelationField) field).isLazy()) {
            getter.annotate(LazyLoaded.class);
        }
    }

    protected void addModifiers(final JDefinedClass entityClass, final Field field) throws JClassAlreadyExistsException {
        final Boolean collection = field.isCollection();
        if (collection != null && collection) {
            codeGenerator.addAddMethod(entityClass, field);
            codeGenerator.addRemoveMethod(entityClass, field);
        }
    }

}
