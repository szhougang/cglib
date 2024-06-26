/*
 * Copyright 2003 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.cglib.transform.impl;

import java.lang.reflect.Constructor;

import org.objectweb.asm.Type;
import net.sf.cglib.core.Block;
import net.sf.cglib.core.CodeEmitter;
import net.sf.cglib.core.Constants;
import net.sf.cglib.core.EmitUtils;
import net.sf.cglib.core.Signature;
import net.sf.cglib.core.TypeUtils;
import net.sf.cglib.transform.ClassEmitterTransformer;

@SuppressWarnings("rawtypes")
public class UndeclaredThrowableTransformer extends ClassEmitterTransformer {

    private final Type wrapper;

    public UndeclaredThrowableTransformer(Class wrapper) {
        this.wrapper = Type.getType(wrapper);
        boolean found = false;
        Constructor[] cstructs = wrapper.getConstructors();
        for (Constructor cstruct : cstructs) {
            Class[] types = cstruct.getParameterTypes();
            if (types.length == 1 && types[0].equals(Throwable.class)) {
                found = true;
                break;
            }
        }
        if (!found) {
			throw new IllegalArgumentException(wrapper + " does not have a single-arg constructor that takes a Throwable");
		}
    }

    @Override
    public CodeEmitter begin_method(int access, final Signature sig, final Type[] exceptions) {
        CodeEmitter e = super.begin_method(access, sig, exceptions);
        if (TypeUtils.isAbstract(access) || sig.equals(Constants.SIG_STATIC)) {
            return e;
        }
        return new CodeEmitter(e) {
	        private final boolean isConstructor = Constants.CONSTRUCTOR_NAME.equals(sig.getName());
            private Block handler = begin_block();
	        private boolean callToSuperSeen;
	        @Override
	        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
		        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
		        if (isConstructor && !callToSuperSeen && Constants.CONSTRUCTOR_NAME.equals(name)) {
			        // we start the entry in the exception table after the call to super
			        handler = begin_block();
			        callToSuperSeen = true;
		        }
	        }
            @Override
            public void visitMaxs(int maxStack, int maxLocals) {
                handler.end();
                EmitUtils.wrap_undeclared_throwable(this, handler, exceptions, wrapper);
                super.visitMaxs(maxStack, maxLocals);
            }
        };
    }
}
