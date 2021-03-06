/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intellij.execution.junit.codeInsight.references;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.*;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.psi.filters.position.FilterPattern;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class JUnitReferenceContributor extends PsiReferenceContributor {
  private static PsiElementPattern.Capture<PsiLiteral> getElementPattern(String annotation, String paramName) {
    return PlatformPatterns.psiElement(PsiLiteral.class).and(new FilterPattern(new TestAnnotationFilter(annotation, paramName)));
  }

  public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
    registrar.registerReferenceProvider(getElementPattern("org.junit.jupiter.params.provider.MethodSource", "names"), new PsiReferenceProvider() {
      @NotNull
      public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull final ProcessingContext context) {
        return new MethodSourceReference[]{new MethodSourceReference((PsiLiteral)element)};
      }
    });
  }

  private static class TestAnnotationFilter implements ElementFilter {

    private final String myAnnotation;
    private final String myParameterName;

    public TestAnnotationFilter(String annotation, @NotNull @NonNls String parameterName) {
      myAnnotation = annotation;
      myParameterName = parameterName;
    }

    public boolean isAcceptable(Object element, PsiElement context) {
      PsiNameValuePair pair = PsiTreeUtil.getParentOfType(context, PsiNameValuePair.class, false, PsiMember.class, PsiStatement.class);
      if (pair == null) return false;
      if (!myParameterName.equals(pair.getName())) return false;
      PsiAnnotation annotation = PsiTreeUtil.getParentOfType(pair, PsiAnnotation.class);
      if (annotation == null) return false;
      return myAnnotation.equals(annotation.getQualifiedName());
    }

    public boolean isClassAcceptable(Class hintClass) {
      return PsiLiteral.class.isAssignableFrom(hintClass);
    }
  }
}
