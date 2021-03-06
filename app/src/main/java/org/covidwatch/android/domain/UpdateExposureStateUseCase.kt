package org.covidwatch.android.domain

import androidx.work.*
import org.covidwatch.android.exposurenotification.ENStatus
import org.covidwatch.android.extension.toResult
import org.covidwatch.android.functional.Either
import org.covidwatch.android.work.UpdateExposureStateWork
import timber.log.Timber
import java.util.*

class UpdateExposureStateUseCase(
    private val workManager: WorkManager,
    dispatchers: AppCoroutineDispatchers
) : UseCase<UUID, UpdateExposureStateUseCase.Params>(dispatchers) {
    override suspend fun run(params: Params?): Either<ENStatus, UUID> {
        params ?: return Either.Left(ENStatus.Failed)
        Timber.d("Start ${javaClass.simpleName} for token: ${params.token}")

        val updateWork = OneTimeWorkRequestBuilder<UpdateExposureStateWork>()
            .setInputData(
                Data.Builder().putString(UpdateExposureStateWork.PARAM_TOKEN, params.token).build()
            )
            .build()

        workManager.enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.APPEND,
            updateWork
        )
        val workInfoLiveData = workManager.getWorkInfoById(updateWork.id)
        val workInfo = workInfoLiveData.await()
        return workInfo.toResult()
    }

    data class Params(val token: String)

    companion object {
        const val WORK_NAME = "update_exposure_state"
    }
}