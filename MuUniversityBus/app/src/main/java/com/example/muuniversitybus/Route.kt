package com.example.muuniversitybus

data class Route(
    val id: String = "",         // Unique route ID
    val name: String = "",       // Route name
    val imageResId: Int = 0      // Drawable resource ID
)


val itemList = listOf(
    Route(
        id = "01",
        name = "mu_arid-arid_haki",
        imageResId = R.drawable.mu_arid_adihaki
    ),

    Route(
        id = "02",
        name = "mu_arid-romanat",
        imageResId = R.drawable.mu_arid_romanat
    ),
    Route(id = "03", name = "Mu_arid-ayder", imageResId = R.drawable.mu_arid_aider)

)
