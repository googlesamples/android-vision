package com.google.android.gms.samples.vision.face.facetracker.api.api.response.model

data class SignData(val Employees:List<Employee>) {
    data class Employee(val EmployeeId:String
                        , val MallId:String
                        , val BranchId:String
                        , val DeptId:String
                        , val EmployeeIdentifyId:String
                        , val EmployeeNo:String
                        , val EmployeeName:String
                        , val Description:String
                        , val SignType:String
                        , val SignDateTime:String
                        , val PersonId:String
                        , val FaceId:String) {
        fun getTrimedSignDateTime():String = SignDateTime.run {
            // ingore all string at last dot "."
            SignDateTime.substring(0, SignDateTime.lastIndexOf("."))
                        .replace("T", " ")
        }
    }
}