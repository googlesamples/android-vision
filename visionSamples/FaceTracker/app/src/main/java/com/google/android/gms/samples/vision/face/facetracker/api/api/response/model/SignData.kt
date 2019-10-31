package com.google.android.gms.samples.vision.face.facetracker.api.api.response.model

data class SignData(val SignId:String
                        , val OrgId:String
                        , val OrgName:String
                        , val MallId:String
                        , val MallName:String
                        , val BranchId:String
                        , val BranchName:String
                        , val DeptId:String
                        , val DeptName:String
                        , val EmployeeNo:String
                        , val EmployeeId:String
                        , val EmployeeName:String
                        , val SignDateTime:String
                        , val SignType:String
                        , val CameraName:String
                        , val ImageUrl:String) {
    fun getTrimedSignDateTime():String = SignDateTime.run {
        // ingore all string at last dot "."
        SignDateTime.substring(0, SignDateTime.lastIndexOf("."))
            .replace("T", " ")
    }
}