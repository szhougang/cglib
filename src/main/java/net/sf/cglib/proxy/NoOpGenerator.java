/*
 * Copyright 2003,2004 The Apache Software Foundation
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
package net.sf.cglib.proxy;

import java.util.Iterator;
import java.util.List;

import net.sf.cglib.core.ClassEmitter;
import net.sf.cglib.core.CodeEmitter;
import net.sf.cglib.core.EmitUtils;
import net.sf.cglib.core.MethodInfo;
import net.sf.cglib.core.TypeUtils;

@SuppressWarnings({"rawtypes", "unchecked"})
class NoOpGenerator
implements CallbackGenerator
{
    public static final NoOpGenerator INSTANCE = new NoOpGenerator();

    @Override
	public void generate(ClassEmitter ce, Context context, List methods) {
        for (Iterator it = methods.iterator(); it.hasNext();) {
            MethodInfo method = (MethodInfo)it.next();
            if (TypeUtils.isBridge(method.getModifiers()) || (
                    TypeUtils.isProtected(context.getOriginalModifiers(method)) &&
                    TypeUtils.isPublic(method.getModifiers()))) {
                CodeEmitter e = EmitUtils.begin_method(ce, method);
                e.load_this();
                context.emitLoadArgsAndInvoke(e, method);
                e.return_value();
                e.end_method();
            }
        }
    }

    @Override
	public void generateStatic(CodeEmitter e, Context context, List methods) { }
}
