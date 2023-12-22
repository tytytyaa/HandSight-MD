//package com.example.handsight
//
//fun Helper() {
//    // ... (after running model inference)
//
//    val modelOutput = mo.run(inputImage) as FloatArray // Assuming modelOutput is the array of 26 values
//    var highestProbabilityIndex = 0f // Assuming index 0 has the highest probability initially
//    var highestProbability = modelOutput[0]
//
//    for (i in 1 until modelOutput.size) {
//        if (modelOutput[i] > highestProbability) {
//            highestProbabilityIndex = i.toFloat()
//            highestProbability = modelOutput[i]
//        }
//    }
//
//    val predictedLetter = 'A'.plus(highestProbabilityIndex.toInt())
//
//// Display the predicted letter in your UI
//    outputTextView.text = "Predicted Letter: $predictedLetter"
//}