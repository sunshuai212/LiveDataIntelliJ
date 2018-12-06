package com.chomper.livedata

import com.intellij.psi.*
import com.intellij.usages.Usage
import com.intellij.usages.UsageInfo2UsageAdapter

/**
 * Created by kgmyshin on 2015/06/07.
 */
class ReceiverFilter : Filter {
    override fun shouldShow(usage: Usage): Boolean {
        var element = (usage as UsageInfo2UsageAdapter).element
        if (element is PsiJavaCodeReferenceElement) {
            if (element is PsiTypeElement) {
                if ((element.getParent()) is PsiParameter) {
                    if ((element.getParent().parent) is PsiParameterList) {
                        if ((element.getParent().parent.parent) is PsiMethod) {
                            val method = element.getParent().parent.parent as PsiMethod
                            if (PsiUtils.isEventBusReceiver(method)) {
                                return true
                            }
                        }
                    }
                }
            }
        }
        return false
    }
}
