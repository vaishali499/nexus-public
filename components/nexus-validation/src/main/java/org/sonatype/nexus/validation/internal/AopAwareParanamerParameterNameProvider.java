/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.validation.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

import com.thoughtworks.paranamer.Paranamer;
import org.hibernate.validator.parameternameprovider.ParanamerParameterNameProvider;

/**
 * AOP-aware {@link ParanamerParameterNameProvider}.
 *
 * @since 3.0
 */
public class AopAwareParanamerParameterNameProvider
  extends ParanamerParameterNameProvider
{
  public AopAwareParanamerParameterNameProvider() {
    // empty
  }

  public AopAwareParanamerParameterNameProvider(final Paranamer paranamer) {
    super(paranamer);
  }

  /**
   * Class-name token which indicates class is synthetic generated by Guice AOP.
   */
  private static final String GUICE_ENHANCED = "$$EnhancerByGuice$$";

  private static boolean isEnhancedSubclass(final Class<?> type) {
    return type.getName().contains(GUICE_ENHANCED);
  }

  @Override
  public List<String> getParameterNames(final Constructor<?> constructor) {
    return super.getParameterNames(resolve(constructor));
  }

  /**
   * Resolve constructor for synthetic classes generated by AOP platforms.
   */
  private Constructor<?> resolve(final Constructor<?> constructor) {
    Class<?> type = constructor.getDeclaringClass();
    if (isEnhancedSubclass(type)) {
      try {
        return type.getSuperclass().getDeclaredConstructor(constructor.getParameterTypes());
      }
      catch (NoSuchMethodException e) {
        // ignore
      }
    }
    return constructor;
  }

  @Override
  public List<String> getParameterNames(final Method method) {
    return super.getParameterNames(resolve(method));
  }

  /**
   * Resolve method for synthetic classes generated by AOP platforms.
   */
  private Method resolve(final Method method) {
    Class<?> type = method.getDeclaringClass();
    if (isEnhancedSubclass(type)) {
      try {
        return type.getSuperclass().getDeclaredMethod(method.getName(), method.getParameterTypes());
      }
      catch (NoSuchMethodException e) {
        // ignore
      }
    }
    return method;
  }
}
