package com.google.android.gms.samples.vision.face.facetracker.api.api.response.model

import com.google.gson.annotations.SerializedName

data class SignInOutInfo(@SerializedName("Employee_Id") val EmployeeId:String
                         , @SerializedName("Sign_DateTime") val SignDateTime:String
                         , @SerializedName("Sign_Date") val SignDate:String
                         , @SerializedName("Sign_Type") val SignType:String
                         , @SerializedName("Org_Id") val OrgId:String
                         , @SerializedName("Org_Name") val OrgName:String
                         , @SerializedName("Dept_No") val DeptNo:String
                         , @SerializedName("Dept_Name") val DeptName:String
                         , @SerializedName("Employee_Name") val EmployeeName:String
                         , @SerializedName("Employee_No") val EmployeeNo:String
                         , @SerializedName("Image_Url") val ImageUrl:String
                         , @SerializedName("Mall_Id") val MallId:String
                         , @SerializedName("Mall_Name") val MallName:String
                         , @SerializedName("Branch_Id") val BranchId:String
                         , @SerializedName("Branch_Name") val BranchName:String?) {
    fun getTrimedSignDateTime():String = SignDateTime.run {
        // ingore all string at last dot "."
        SignDateTime.substring(0, SignDateTime.lastIndexOf("."))
            .replace("T", " ")
    }
}