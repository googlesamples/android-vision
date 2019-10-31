package com.google.android.gms.samples.vision.face.facetracker.api.api.interf

import com.example.test.api.response.ApiResponse
import com.google.android.gms.samples.vision.face.facetracker.api.api.response.model.SignData
import com.google.android.gms.samples.vision.face.facetracker.api.api.response.model.SignInData
import com.google.android.gms.samples.vision.face.facetracker.api.api.response.model.SignInOutInfo
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.http.*

interface IFaceLink {
    @Headers(
        "OrgId: 0B193586CD103FA7",
        "ConsoleName: LabConsole1",
        "CameraName: Camera19"
    )
    @POST("/api/Face/SignIn")
    fun sign(@Header("SignType") signType:String, @Body faceImg: RequestBody):Observable<SignInData>

    @GET("/api/Report/v1.0/GetSignInOutList")
    fun getSignInOutList(@Query("orgId") orgId:String, @Query("employeeId") employeeId:String, @Query("startDate") startDate:String, @Query("endDate") endDate:String):Observable<ApiResponse<List<SignInOutInfo>>>

    @GET("/api/Report/v1.0/GetTimeLine")
    fun getTimeLine(@Query("orgId") orgId:String, @Query("startDate") startDate:String, @Query("endDate") endDate:String):Observable<ApiResponse<String>>

    @GET("/api/Report/v1.0/GetSignList")
    fun getSignList(@Query("orgId") orgId:String, @Query("employeeId") employeeId:String, @Query("startDate") startDate:String, @Query("endDate") endDate:String, @Query("signType") signType:String):Observable<ApiResponse<List<SignData>>>

}