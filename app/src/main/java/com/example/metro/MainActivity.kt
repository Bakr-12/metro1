package com.example.metro
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    private val line1Stations = listOf(
        "Helwan", "Ain Helwan", "Helwan University", "Wadi Hof", "Hadayek Helwan", "El-Masraa", "Tura El-Esmant",
        "Kozzika", "Tora El-Balad", "Sakanat El-Maadi", "Maadi", "Hadayek El-Maadi", "Dar El-Salam", "El-Zahraa",
        "Mar Girgis", "El-Malek El-Saleh", "Al-Sayeda Zeinab", "Saad Zaghloul", "Sadat", "Nasser", "Orabi", "Al-Shohada",
        "Ghamra", "El-Demerdash", "Manshiet El-Sadr", "Kobri El-Qobba", "Hammamat El-Qobba", "Saray El-Qobba", "Hadayek El-Zaitoun",
        "Helmeyet El-Zaitoun", "Rabi'ya", "Ain Shams", "Ezbet El-Nakhl", "El-Marg", "New El-Marg"
    )

    private val line2Stations = listOf(
        "Shubra El-Kheima", "Kolleyyet El-Zeraa", "Mezallat", "Khalafawy", "St. Teresa", "Rod El-Farag", "Masarra",
        "Al-Shohada", "Attaba", "Mohamed Naguib", "Sadat", "Opera", "Dokki", "El Bohouth", "Cairo University",
        "Faisal", "Giza", "Omm El-Masryeen", "Sakiat Mekky", "El-Mounib"
    )

    private val line3Stations = listOf(
        "Adly Mansour", "Haykestep", "Omar Ibn El-Khattab", "Qobaa", "Hesham Barakat", "El-Nozha", "El-Shams Club",
        "Alf Maskan", "Heliopolis Square", "Al-Ahram", "Haroun", "Stadium", "Fair Zone", "Abbassiya", "Abdou Pasha",
        "El-Geish", "Bab El-Shaaria", "Attaba", "Nasser", "Maspero", "Safaa Hijazy", "Kit Kat", "Sudan", "Imbaba",
        "El-Bohy", "El-Qawmia", "Ring Road", "Rawd Al-Farag Corridor"
    )

    private val line4Stations = listOf(
        "Adly Mansour", "Haykestep", "Omar Ibn El-Khattab", "Qobaa", "Hesham Barakat",
        "El-Nozha", "El-Shams Club", "Alf Maskan", "Heliopolis Square", "Al-Ahram", "Haroun",
        "Stadium", "Fair Zone", "Abbassiya", "Abdou Pasha", "El-Geish", "Bab El-Shaaria",
        "Attaba", "Nasser", "Maspero", "Safaa Hijazy", "Kit Kat", "Tawfiqiya",
        "Wadi El Nil", "Gamet El Dowel", "Boulak El Dakrour", "Cairo University"
    )

    private val transferStations = listOf("Sadat", "Al-Shohada", "Nasser", "Attaba", "Kit Kat", "Cairo University")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val startSpinner: Spinner = findViewById(R.id.startSpinner)
        val endSpinner: Spinner = findViewById(R.id.endSpinner)
        val swapButton: Button = findViewById(R.id.swapButton)
        val findRouteButton: Button = findViewById(R.id.findRouteButton)

        val allStations = (line1Stations + line2Stations + line3Stations + line4Stations).distinct().sorted()

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, allStations)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        startSpinner.adapter = adapter
        endSpinner.adapter = adapter

        swapButton.setOnClickListener {
            val startPosition = startSpinner.selectedItemPosition
            val endPosition = endSpinner.selectedItemPosition

            startSpinner.setSelection(endPosition)
            endSpinner.setSelection(startPosition)
        }

        findRouteButton.setOnClickListener {
            it.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100)
                .withEndAction {
                    it.animate().scaleX(1f).scaleY(1f).setDuration(100)
                }
            val start = startSpinner.selectedItem.toString()
            val end = endSpinner.selectedItem.toString()
            val result = findRoute(start, end)
            showResult(result)
        }
    }

    fun findRoute(start: String, end: String): String {
        if (start == end) {
            return "Start Station and End Station can't be the same."
        }

        val startLines = findStationLines(start)
        val endLines = findStationLines(end)

        if (startLines.isEmpty() || endLines.isEmpty()) {
            return "One or both of the stations are invalid."
        }

        val commonLines = startLines.intersect(endLines)
        if (commonLines.isNotEmpty()) {
            val line = commonLines.first()
            return calculateDirectRoute(start, end, line)
        }

        val possibleTransfers = transferStations.filter { transfer ->
            startLines.any { it.contains(transfer) } && endLines.any { it.contains(transfer) }
        }

        if (possibleTransfers.isEmpty()) {
            return "No valid transfer station between the lines."
        }

        var optimalTransfer: String? = null
        var minTotalStations = Int.MAX_VALUE

        for (transfer in possibleTransfers) {
            val transferStartLines = startLines.filter { it.contains(transfer) }
            val transferEndLines = endLines.filter { it.contains(transfer) }

            for (startLine in transferStartLines) {
                for (endLine in transferEndLines) {
                    val segment1 = calculateRouteSegment(start, transfer, startLine)
                    val segment2 = calculateRouteSegment(transfer, end, endLine)

                    val totalStations = segment1.size + segment2.size

                    if (totalStations < minTotalStations) {
                        minTotalStations = totalStations
                        optimalTransfer = transfer
                    }
                }
            }
        }

        if (optimalTransfer == null) {
            return "No valid transfer station between the lines."
        }

        val transferStartLines = startLines.filter { it.contains(optimalTransfer) }
        val transferEndLines = endLines.filter { it.contains(optimalTransfer) }

        val startLine = transferStartLines.first()
        val endLine = transferEndLines.first()

        val segment1 = calculateRouteSegment(start, optimalTransfer, startLine)
        val segment2 = calculateRouteSegment(optimalTransfer, end, endLine)

        val routeStations = mutableListOf<String>()
        routeStations.add(start)
        routeStations.addAll(segment1)

        if (segment2.isNotEmpty()) {
            when {
                segment2.first() == optimalTransfer -> {
                    routeStations.addAll(segment2.drop(1))
                }
                segment2.last() == optimalTransfer -> {
                    routeStations.addAll(segment2.dropLast(1))
                }
                else -> {
                    routeStations.addAll(segment2)
                }
            }
        }

        val numberOfStations = routeStations.size - 1
        val travelTime = numberOfStations * 2
        val price = calculateTicketPrice(numberOfStations)

//Donia  لون النتايج يبقي احمر بس سيبي الروت اسود
        // لازم تيكست فيو
        return """
            Route: ${routeStations.joinToString(" -> ")}
            Number of stations: $numberOfStations
            Estimated travel time: $travelTime minutes
            Ticket Price: $price EGP
        """.trimIndent()
    }

    fun findStationLines(station: String): List<List<String>> {
        return listOf(line1Stations, line2Stations, line3Stations, line4Stations).filter { it.contains(station) }
    }

    private fun calculateDirectRoute(start: String, end: String, line: List<String>): String {
        val startIndex = line.indexOf(start)
        val endIndex = line.indexOf(end)

        if (startIndex == -1 || endIndex == -1) {
            return "One or both of the stations are invalid on the selected line."
        }

        val routeStations = if (startIndex < endIndex) {
            line.subList(startIndex, endIndex + 1)
        } else {
            line.subList(endIndex, startIndex + 1).reversed()
        }

        val numberOfStations = routeStations.size - 1
        val travelTime = numberOfStations * 2
        val price = calculateTicketPrice(numberOfStations)

        return """
          Route: ${routeStations.joinToString(" -> ")}
            Number of stations: $numberOfStations
            Estimated travel time: $travelTime minutes
            Ticket Price: $price EGP
        """.trimIndent()
    }

    private fun calculateRouteSegment(start: String, end: String, line: List<String>): List<String> {
        val startIndex = line.indexOf(start)
        val endIndex = line.indexOf(end)

        if (startIndex == -1 || endIndex == -1) {
            return emptyList()
        }

        return if (startIndex < endIndex) {
            line.subList(startIndex + 1, endIndex + 1)
        } else {
            line.subList(endIndex, startIndex).reversed()
        }
    }

    private fun calculateTicketPrice(numberOfStations: Int): Int {
        if (numberOfStations in 1..9) {
            return 8
        } else if (numberOfStations in 10..16) {
            return 10
        } else if (numberOfStations in 17..23) {
            return 15
        } else if (numberOfStations > 23) {
            return 20
        } else {
            return 5
        }
    }

    private fun showResult(result: String) {
        AlertDialog.Builder(this)
            .setTitle("Route Result")
            .setMessage(result)
            .setPositiveButton("OK", null)
            .show()
    }
}
