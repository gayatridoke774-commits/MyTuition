package com.example.mytuition.core.data.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface PocketBaseApi {

    // === AUTHENTICATION ===
    @POST("collections/users/auth-with-password")
    suspend fun loginWithPassword(
        @Body body: Map<String, String>
    ): Response<PbAuthResponse>

    // === USER RECORD ===
    @GET("collections/users/records/{id}")
    suspend fun getUserRecord(
        @Path("id") id: String
    ): Response<PbUserRecord>

    @PATCH("collections/users/records/{id}")
    suspend fun updateUserRecord(
        @Path("id") id: String,
        @Body body: Map<String, Any?>
    ): Response<PbUserRecord>

    // === CUSTOM MYTUITION ROUTES ===
    @POST("mytuition/register-student")
    suspend fun registerStudent(
        @Body body: PbRegisterStudentRequest
    ): Response<PbRegisterStudentResponse>

    @GET("mytuition/student-credentials/{studentId}")
    suspend fun getStudentCredentials(
        @Path("studentId") studentId: String
    ): Response<PbCredentialsResponse>

    @POST("mytuition/reset-password/{studentId}")
    suspend fun resetPassword(
        @Path("studentId") studentId: String,
        @Body body: PbResetPasswordRequest
    ): Response<PbCredentialsResponse>

    @POST("mytuition/mark-attendance")
    suspend fun markAttendance(
        @Body body: PbMarkAttendanceRequest
    ): Response<Map<String, Any>>

    @POST("mytuition/create-homework")
    suspend fun createHomework(
        @Body body: PbCreateHomeworkRequest
    ): Response<PbCreateHomeworkResponse>

    @POST("mytuition/mark-fee-paid")
    suspend fun markFeePaid(
        @Body body: PbMarkFeePaidRequest
    ): Response<PbMarkFeePaidResponse>

    @POST("mytuition/mark-fee-pending")
    suspend fun markFeePending(
        @Body body: PbMarkFeePendingRequest
    ): Response<PbMarkFeePendingResponse>

    // === HOME DATA ===
    @GET("mytuition/home-data")
    suspend fun getStudentHomeData(
        @Query("date") date: String
    ): Response<PbStudentHomeDataResponse>

    @GET("mytuition/home-data")
    suspend fun getTeacherHomeData(
        @Query("date") date: String
    ): Response<PbTeacherHomeDataResponse>

    // === TEACHER EARNINGS & SHORTNAME ===
    @GET("mytuition/teacher-earnings")
    suspend fun getTeacherEarnings(
        @Query("month") month: String,
        @Query("year") year: String
    ): Response<PbTeacherEarningsResponse>

    @GET("mytuition/check-shortname/{name}")
    suspend fun checkShortname(
        @Path("name") name: String
    ): Response<PbCheckShortnameResponse>

    // === POCKETBASE COLLECTIONS REST API ===
    @GET("collections/homework/records")
    suspend fun getHomeworkList(
        @Query("filter") filter: String? = null,
        @Query("expand") expand: String? = "batch,subject,teacher",
        @Query("sort") sort: String? = "-dueDate"
    ): Response<PbListResponse<PbHomeworkRecord>>

    @GET("collections/homework/records/{id}")
    suspend fun getHomeworkDetail(
        @Path("id") id: String,
        @Query("expand") expand: String? = "batch,subject,teacher"
    ): Response<PbHomeworkRecord>

    @GET("collections/homework_submissions/records")
    suspend fun getHomeworkSubmissions(
        @Query("filter") filter: String? = null,
        @Query("expand") expand: String? = "student,homework",
        @Query("sort") sort: String? = "-created"
    ): Response<PbListResponse<PbHomeworkSubmissionRecord>>

    @PATCH("collections/homework_submissions/records/{id}")
    suspend fun updateHomeworkSubmission(
        @Path("id") id: String,
        @Body body: Map<String, Any?>
    ): Response<PbHomeworkSubmissionRecord>

    @GET("collections/batches/records")
    suspend fun getBatches(
        @Query("filter") filter: String? = null,
        @Query("expand") expand: String? = "subject,teacher",
        @Query("sort") sort: String? = "name"
    ): Response<PbListResponse<PbBatchRecord>>

    @GET("collections/subjects/records")
    suspend fun getSubjects(
        @Query("filter") filter: String? = null,
        @Query("sort") sort: String? = "name"
    ): Response<PbListResponse<PbSubjectRecord>>

    @GET("collections/fees/records")
    suspend fun getFees(
        @Query("filter") filter: String? = null,
        @Query("expand") expand: String? = "batch,student",
        @Query("sort") sort: String? = "-dueDate"
    ): Response<PbListResponse<PbFeeRecord>>

    @GET("collections/users/records")
    suspend fun getUsers(
        @Query("filter") filter: String? = null,
        @Query("sort") sort: String? = "name"
    ): Response<PbListResponse<PbUserRecord>>

    @GET("collections/announcements/records")
    suspend fun getAnnouncements(
        @Query("filter") filter: String? = null,
        @Query("sort") sort: String? = "-date"
    ): Response<PbListResponse<PbAnnouncementRecord>>

    @POST("collections/batches/records")
    suspend fun createBatch(
        @Body body: PbCreateBatchRequest
    ): Response<PbBatchRecord>

    @POST("collections/announcements/records")
    suspend fun createAnnouncement(
        @Body body: Map<String, Any?>
    ): Response<PbAnnouncementRecord>

    @PATCH("collections/class_sessions/records/{id}")
    suspend fun updateClassSession(
        @Path("id") id: String,
        @Body body: Map<String, Any?>
    ): Response<Map<String, Any>>

    @GET("collections/attendance/records")
    suspend fun getAttendance(
        @Query("filter") filter: String? = null,
        @Query("sort") sort: String? = "-date",
        @Query("perPage") perPage: Int = 100
    ): Response<PbListResponse<PbAttendanceRecord>>

    @GET("collections/resources/records")
    suspend fun getResources(
        @Query("filter") filter: String? = null,
        @Query("sort") sort: String? = "-created",
        @Query("perPage") perPage: Int = 50
    ): Response<PbListResponse<PbResourceRecord>>

    @POST("mytuition/remind-pending-homework")
    suspend fun remindPendingHomework(
        @Body body: PbRemindPendingHomeworkRequest
    ): Response<Map<String, Any>>

    @POST("collections/homework_submissions/records")
    suspend fun createHomeworkSubmission(
        @Body body: Map<String, Any?>
    ): Response<PbHomeworkSubmissionRecord>

    @GET("collections/users/records/{id}")
    suspend fun getUser(
        @Path("id") id: String,
        @Query("expand") expand: String? = "institute,batches"
    ): Response<PbUserRecord>

    @PATCH("collections/users/records/{id}")
    suspend fun updateUser(
        @Path("id") id: String,
        @Body body: Map<String, Any?>
    ): Response<PbUserRecord>

    @GET("collections/class_sessions/records/{id}")
    suspend fun getClassSession(
        @Path("id") id: String,
        @Query("expand") expand: String? = "subject,room,teacher,batch"
    ): Response<PbClassSessionRecord>

    @GET("collections/class_sessions/records")
    suspend fun getClassSessions(
        @Query("filter") filter: String? = null,
        @Query("expand") expand: String? = "subject,room,teacher,batch",
        @Query("sort") sort: String? = "date,startTime",
        @Query("perPage") perPage: Int = 50
    ): Response<PbListResponse<PbClassSessionRecord>>

    // === RESOURCE FILE DOWNLOAD ===
    @Streaming
    @GET
    suspend fun downloadResource(
        @Url fileUrl: String
    ): Response<ResponseBody>
}
