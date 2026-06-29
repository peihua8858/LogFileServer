package com.peihua8858.logfileserver.utils

val String.extension: String
get() = substringAfterLast('.', "")