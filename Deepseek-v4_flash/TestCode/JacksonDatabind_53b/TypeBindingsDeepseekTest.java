package com.fasterxml.jackson.databind.type;

import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.TypeVariable;
import java.util.*;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeBindings.TypeParamStash;

public class TypeBindingsDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Target: TypeBindings class - resolves type parameters for generic classes.
     * 
     * Known Defect: TypeRefinementForMap1215Test::testMapRefinement fails with
     * JsonMappingException "Can not construct instance of ... HasUniqueId, problem: abstract types..."
     * This indicates that when resolving type bindings for a Map with a value type that is abstract,
     * the binding resolution fails to properly handle the abstract type, likely due to incorrect
     * handling of unbound variables or recursive type resolution.
     * 
     * Branches targeted:
     * 1. Constructor validation: names.length != types.length -> IllegalArgumentException
     * 2. create(Class, List) with null/empty list -> NO_TYPES
     * 3. create(Class, JavaType[]) with null/empty -> NO_TYPES
     * 4. create(Class, JavaType) with 1 type param - checks varLen != 1
     * 5. create(Class, JavaType, JavaType) with 2 type params - checks varLen != 2
     * 6. createIfNeeded with 0 type params -> emptyBindings
     * 7. withUnboundVariable - creates new array with additional unbound variable
     * 8. findBoundType - loops through names, handles ResolvedRecursiveType
     * 9. isEmpty, size, getBoundName, getBoundType - boundary checks
     * 10. getTypeParameters - empty vs non-empty
    11. hasUnbound - checks unbound variables array
    12. equals - null, different class, different size, different types
    13. hashCode - computed from types
    14. toString - empty vs non-empty
     * 
     * Boundary conditions:
     * - null names/types arrays
     * - empty arrays
     * - index -1, 0, length-1, length
     * - null type arguments
     * - abstract types in Map value position (defect)
     */
    
    // Test helper to create a simple JavaType implementation for testing
    private static class SimpleJavaType extends JavaType {
        private static final long serialVersionUID = 1L;
        private final Class<?> raw;
        private final String sig;
        
        SimpleJavaType(Class<?> raw) {
            super(raw, raw.getTypeParameters().length, Object.class, null, false);
            this.raw = raw;
            this.sig = raw.getName();
        }
        
        @Override
        public JavaType withTypeHandler(Object h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Object h) { return this; }
        @Override
        public JavaType withValueHandler(Object h) { return this; }
        @Override
        public JavaType withContentValueHandler(Object h) { return this; }
        @Override
        public JavaType withStaticTyping() { return this; }
        @Override
        public JavaType withContentType(JavaType contentType) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Object h) { return this; }
        @Override
        public JavaType withValueHandler(Object h) { return this; }
        @Override
        public JavaType withContentValueHandler(Object h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withContentValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType withStaticTyping(Class<?> h) { return this; }
        @Override
        public JavaType withContentType(Class<?> h) { return this; }
        @Override
        public JavaType withContentTypeHandler(Class<?> h) { return this; }
        @Override
        public JavaType withValueHandler(Class<?> h) { return this; }
        @Override
        public JavaType