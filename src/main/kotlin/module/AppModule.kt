package org.delcom.module

import org.delcom.repositories.IPlantRepository
import org.delcom.repositories.PlantRepository
import org.delcom.services.PlantService
import org.delcom.services.ProfileService
import org.koin.dsl.module
import org.delcom.repositories.ISportRepository
import org.delcom.repositories.SportRepository
import org.delcom.services.SportService
import org.koin.dsl.module

val appModule = module {
    // Plant Repository
    single<IPlantRepository> {
        PlantRepository()
    }

    // Plant Service
    single {
        PlantService(get())
    }
    // Novel Repository
    single<ISportRepository> {
        SportRepository()
    }

    // Novel Service
    single {
        SportService(get())
    }

    // Profile Service
    single {
        ProfileService()
    }
}