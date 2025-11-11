package com.runanywhere.startup_hackathon20

import android.app.Application
import android.util.Log
import com.runanywhere.sdk.public.RunAnywhere
import com.runanywhere.sdk.data.models.SDKEnvironment
import com.runanywhere.sdk.public.extensions.addModelFromURL
import com.runanywhere.sdk.llm.llamacpp.LlamaCppServiceProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize SDK asynchronously
        GlobalScope.launch(Dispatchers.IO) {
            initializeSDK()
        }
    }

    private suspend fun initializeSDK() {
        try {
            // Step 1: Initialize SDK
            Log.i("MyApp", "🚀 Initializing RunAnywhere SDK...")
            RunAnywhere.initialize(
                context = this@MyApplication,
                apiKey = "dev",  // Any string works in dev mode
                environment = SDKEnvironment.DEVELOPMENT
            )

            // Wait a moment to ensure SDK initialization completes
            delay(500)

            // IMPORTANT: SDK clears model cache during initialization
            // We need to re-register models AFTER SDK init completes
            Log.i("MyApp", "✅ SDK initialized, now registering models...")

            // Step 2: Register LLM Service Provider
            LlamaCppServiceProvider.register()
            Log.i("MyApp", "✅ LlamaCpp Service Provider registered")

            // Step 3: Register Models (after SDK init to prevent cache clearing)
            registerModels()

            // Step 4: Scan for previously downloaded models
            RunAnywhere.scanForDownloadedModels()
            Log.i("MyApp", "✅ Scanned for downloaded models")

            Log.i("MyApp", "🎉 SDK initialization complete!")

        } catch (e: Exception) {
            Log.e("MyApp", "❌ SDK initialization failed: ${e.message}", e)
            e.printStackTrace()
        }
    }

    private suspend fun registerModels() {
        try {
            // === REGISTER AI MODELS ===
            // SWITCHED TO LLAMA 3.2 1B - More stable than Qwen!
            addModelFromURL(
                url = "https://huggingface.co/bartowski/Llama-3.2-1B-Instruct-GGUF/resolve/main/Llama-3.2-1B-Instruct-Q6_K_L.gguf",
                name = "Llama 3.2 1B Instruct Q6_K",
                type = "LLM"
            )
            Log.d("MyApplication", "📥 Registered AI Model: Llama 3.2 1B (815 MB)")
            Log.d("MyApplication", "  → More stable than Qwen")
            Log.d("MyApplication", "  → Better for all difficulty modes")
            Log.d("MyApplication", "  → Less repetition issues")
        } catch (e: Exception) {
            Log.e("MyApplication", "Failed to register Llama model", e)
        }
    }
}
