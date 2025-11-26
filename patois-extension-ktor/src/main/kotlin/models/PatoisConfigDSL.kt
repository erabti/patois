package models

class PatoisConfigDSL {
    companion object {
        const val DEFAULT_HEADER_NAME = "Accept-Language"
    }


    var headerName: String = DEFAULT_HEADER_NAME
}