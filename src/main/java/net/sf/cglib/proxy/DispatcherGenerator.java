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

import org.objectweb.asm.Type;
import net.sf.cglib.core.ClassEmitter;
import net.sf.cglib.core.CodeEmitter;
import net.sf.cglib.core.MethodInfo;
import net.sf.cglib.core.Signature;
import net.sf.cglib.core.TypeUtils;

@SuppressWarnings({"rawtypes", "unchecked"})
class DispatcherGenerator implements CallbackGenerator {
    public static final DispatcherGenerator INSTANCE =
      new DispatcherGenerator(false);
    public static final DispatcherGenerator PROXY_REF_INSTANCE =
      new DispatcherGenerator(true);

    private static final Type DISPATCHER =
      TypeUtils.parseType("net.sf.cglib.proxy.Dispatcher");
    private static final Type PROXY_REF_DISPATCHER =
      TypeUtils.parseType("net.sf.cglib.proxy.ProxyRefDispatcher");
    private static final Signature LOAD_OBJECT =
      TypeUtils.parseSignature("Object loadObject()");
    private static final Signature PROXY_REF_LOAD_OBJECT =
      TypeUtils.parseSignature("Object loadObject(Object)");

    private boolean proxyRef;

    private DispatcherGenerator(boolean proxyRef) {
        this.proxyRef = proxyRef;
    }

    @Override
	public void generate(ClassEmitter ce, Context context, List methods) {
        for (Iterator it = methods.iterator(); it.hasNext();) {
            MethodInfo method = (MethodInfo)it.next();
            if (!TypeUtils.isProtected(method.getModifiers())) {
                CodeEmitter e = context.beginMethod(ce, method);
                context.emitCallback(e, context.getIndex(method));
                if (proxyRef) {
                    e.load_this();
                    e.invoke_interface(PROXY_REF_DISPATCHER, PROXY_REF_LOAD_OBJECT);
                } else {
                    e.invoke_interface(DISPATCHER, LOAD_OBJECT);
                }
                e.checkcast(method.getClassInfo().getType());
                e.load_args();
                e.invoke(method);
                e.return_value();
                e.end_method();
            }
        }
    }

    @Override
	public void generateStatic(CodeEmitter e, Context context, List methods) { }
}
