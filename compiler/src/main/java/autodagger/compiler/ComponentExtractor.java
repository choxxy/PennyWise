package autodagger.compiler;

import com.google.auto.common.MoreElements;
import com.google.auto.common.MoreTypes;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Scope;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import autodagger.AutoComponent;
import autodagger.AutoSubcomponent;
import autodagger.compiler.utils.AutoComponentExtractorUtil;
import dagger.Component;
import dagger.Subcomponent;
import processorworkflow.AbstractExtractor;
import processorworkflow.Errors;
import processorworkflow.ExtractorUtils;

/**
 * @author Lukasz Piliszczuk - lukasz.pili@gmail.com
 */
public class ComponentExtractor extends AbstractExtractor {

    /**
     * The component element represented by @AutoComponent
     * It's either the element itself, or the element of an annotation if the @AutoComponent
     * is applied on the annotation
     */
    private final Element componentElement;

    private TypeMirror targetTypeMirror;
    private List<TypeMirror> dependenciesTypeMirrors;
    private List<TypeMirror> modulesTypeMirrors;
    private List<TypeMirror> superinterfacesTypeMirrors;
    private List<TypeMirror> subcomponentsTypeMirrors;
    private AnnotationMirror scopeAnnotationTypeMirror;

    public ComponentExtractor(Element componentElement, Element element, Types types, Elements elements, Errors errors) {
        super(element, types, elements, errors);
        this.componentElement = componentElement;

        extract();
    }

    @Override
    public void extract() {
        targetTypeMirror = ExtractorUtils.getValueFromAnnotation(element, AutoComponent.class, AutoComponentExtractorUtil.ANNOTATION_TARGET);
        if (targetTypeMirror == null) {
            targetTypeMirror = componentElement.asType();
        }

        dependenciesTypeMirrors = findTypeMirrors(element, AutoComponentExtractorUtil.ANNOTATION_DEPENDENCIES);
        modulesTypeMirrors = findTypeMirrors(element, AutoComponentExtractorUtil.ANNOTATION_MODULES);
        superinterfacesTypeMirrors = findTypeMirrors(element, AutoComponentExtractorUtil.ANNOTATION_SUPERINTERFACES);
        subcomponentsTypeMirrors = findTypeMirrors(element, AutoComponentExtractorUtil.ANNOTATION_SUBCOMPONENTS);

        ComponentExtractor includesExtractor = null;
        TypeMirror includesTypeMirror = ExtractorUtils.getValueFromAnnotation(element, AutoComponent.class, AutoComponentExtractorUtil.ANNOTATION_INCLUDES);
        if (includesTypeMirror != null) {
            Element includesElement = MoreTypes.asElement(includesTypeMirror);
            if (!MoreElements.isAnnotationPresent(includesElement, AutoComponent.class)) {
                errors.getParent().addInvalid(includesElement, "Included element must be annotated with @AutoComponent");
                return;
            }

            if (element.equals(includesElement)) {
                errors.addInvalid("Auto component %s cannot include himself", element.getSimpleName());
                return;
            }

            includesExtractor = new ComponentExtractor(includesElement, includesElement, types, elements, errors.getParent());
            if (errors.getParent().hasErrors()) {
                return;
            }
        }

        if (includesExtractor != null) {
            dependenciesTypeMirrors.addAll(includesExtractor.getDependenciesTypeMirrors());
            modulesTypeMirrors.addAll(includesExtractor.getModulesTypeMirrors());
            superinterfacesTypeMirrors.addAll(includesExtractor.getSuperinterfacesTypeMirrors());
            subcomponentsTypeMirrors.addAll(includesExtractor.getSubcomponentsTypeMirrors());
        }

        scopeAnnotationTypeMirror = findScope();
    }

    private List<TypeMirror> findTypeMirrors(Element element, String name) {
        boolean addsTo = name.equals(AutoComponentExtractorUtil.ANNOTATION_SUBCOMPONENTS);

        List<TypeMirror> typeMirrors = new ArrayList<>();
        List<AnnotationValue> values = ExtractorUtils.getValueFromAnnotation(element, AutoComponent.class, name);
        if (values != null) {
            for (AnnotationValue value : values) {
                if (!validateAnnotationValue(value, name)) {
                    continue;
                }

                try {
                    TypeMirror tm = (TypeMirror) value.getValue();
                    if (addsTo) {
                        Element e = MoreTypes.asElement(tm);
                        if (!MoreElements.isAnnotationPresent(e, AutoSubcomponent.class) && !MoreElements.isAnnotationPresent(e, Subcomponent.class)) {
                            errors.addInvalid("@AutoComponent cannot declare a subcomponent that is not annotated with @Subcomponent or @AutoSubcomponent: %s", e.getSimpleName());
                            continue;
                        }
                    }
                    typeMirrors.add(tm);
                } catch (Exception e) {
                    errors.addInvalid(e.getMessage());
                    break;
                }
            }
        }

        return typeMirrors;
    }

    /**
     * Find annotation that is itself annoted with @Scope
     * If there is one, it will be later applied on the generated component
     * Otherwise the component will be unscoped
     * Throw error if more than one scope annotation found
     */
    private AnnotationMirror findScope() {
        // first look on the @AutoComponent annotated element
        AnnotationMirror annotationMirror = findScope(element);
        if (annotationMirror == null && element != componentElement) {
            // look also on the real component element, if @AutoComponent is itself on
            // an another annotation
            annotationMirror = findScope(componentElement);
        }

        return annotationMirror;
    }

    private AnnotationMirror findScope(Element element) {
        List<AnnotationMirror> annotationMirrors = ExtractorUtil.findAnnotatedAnnotation(element, Scope.class);
        if (annotationMirrors.isEmpty()) {
            return null;
        }

        if (annotationMirrors.size() > 1) {
            errors.getParent().addInvalid(element, "Cannot have several scope (@Scope).");
            return null;
        }

        return annotationMirrors.get(0);
    }

    private boolean validateAnnotationValue(AnnotationValue value, String member) {
        if (!(value.getValue() instanceof TypeMirror)) {
            errors.addInvalid("%s cannot reference generated class. Use the class that applies the @AutoComponent annotation", member);
            return false;
        }

        return true;
    }

    public Element getComponentElement() {
        return componentElement;
    }

    public TypeMirror getTargetTypeMirror() {
        return targetTypeMirror;
    }

    public List<TypeMirror> getDependenciesTypeMirrors() {
        return dependenciesTypeMirrors;
    }

    public List<TypeMirror> getModulesTypeMirrors() {
        return modulesTypeMirrors;
    }

    public List<TypeMirror> getSuperinterfacesTypeMirrors() {
        return superinterfacesTypeMirrors;
    }

    public List<TypeMirror> getSubcomponentsTypeMirrors() {
        return subcomponentsTypeMirrors;
    }

    public AnnotationMirror getScopeAnnotationTypeMirror() {
        return scopeAnnotationTypeMirror;
    }
}
