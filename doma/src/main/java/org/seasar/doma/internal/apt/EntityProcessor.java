/*
 * Copyright 2004-2009 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.doma.internal.apt;

import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic.Kind;

import org.seasar.doma.internal.apt.meta.EntityMeta;
import org.seasar.doma.internal.apt.meta.EntityMetaFactory;
import org.seasar.doma.internal.apt.meta.PropertyMetaFactory;
import org.seasar.doma.message.MessageCode;

/**
 * @author taedium
 * 
 */
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedAnnotationTypes( { "org.seasar.doma.Entity",
        "org.seasar.doma.MappedSuperclass" })
@SupportedOptions( { Options.TEST, Options.DEBUG, Options.SUFFIX })
public class EntityProcessor extends DomaAbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations,
            RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return true;
        }
        for (TypeElement a : annotations) {
            EntityMetaFactory entityMetaFactory = createEntityMetaFactory();
            for (TypeElement entityElement : ElementFilter.typesIn(roundEnv
                    .getElementsAnnotatedWith(a))) {
                try {
                    EntityMeta entityMeta = entityMetaFactory
                            .createEntityMeta(entityElement);
                    if (entityMeta.isMappedSuperclass()) {
                        continue;
                    }
                    generateEntity(entityElement, entityMeta);
                } catch (AptException e) {
                    Notifier.notify(processingEnv, e);
                } catch (AptIllegalStateException e) {
                    Notifier
                            .notify(processingEnv, Kind.ERROR, MessageCode.DOMA4039, entityElement);
                    throw e;
                } catch (RuntimeException e) {
                    Notifier
                            .notify(processingEnv, Kind.ERROR, MessageCode.DOMA4016, entityElement);
                    throw e;
                }
            }
        }
        return true;
    }

    protected EntityMetaFactory createEntityMetaFactory() {
        PropertyMetaFactory propertyMetaFactory = new PropertyMetaFactory(
                processingEnv);
        return new EntityMetaFactory(processingEnv, propertyMetaFactory);
    }

    protected void generateEntity(TypeElement entityElement,
            EntityMeta entityMeta) {
        String qualifiedName = entityElement.getQualifiedName()
                + Options.getSuffix(processingEnv);
        Generator generator = createGenerator(qualifiedName, entityMeta);
        generate(generator, qualifiedName, entityElement);
    }

    protected Generator createGenerator(String qualifiedName,
            EntityMeta entityMeta) {
        return new EntityGenerator(processingEnv, qualifiedName, entityMeta);
    }
}