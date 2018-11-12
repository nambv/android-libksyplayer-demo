package streaming.yeah1.com.streaming.utils

import timber.log.Timber

class LoggerManager : Timber.DebugTree() {

    override fun createStackElementTag(element: StackTraceElement): String {
        val tag = super.createStackElementTag(element)
        val methodName = element.methodName
        val lineNumber = element.lineNumber
        return "$tag $methodName:$lineNumber"
    }
}
