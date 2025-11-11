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
            // SINGLE MODEL: Qwen 2.5 3B (2.3 GB)
            // Used for: ALL difficulty levels (Beginner, Intermediate, Advanced, P2P)
            // This model provides excellent reasoning capabilities across all modes
            addModelFromURL(
                url = "https://huggingface.co/Qwen/Qwen2.5-3B-Instruct-GGUF/resolve/main/qwen2.5-3b-instruct-q6_k.gguf",
                name = "Qwen 2.5 3B Instruct Q6_K",
                type = "LLM"
            )
            Log.d("MyApplication", "📥 Registered AI Model: Qwen 2.5 3B (2.3 GB)")
            Log.d("MyApplication", "🎮 Single Model Strategy:")
            Log.d("MyApplication", "  → Beginner Mode: Qwen 3B (IQ 55-65)")
            Log.d("MyApplication", "  → Intermediate Mode: Qwen 3B (IQ 70-80)")
            Log.d("MyApplication", "  → Advanced Mode: Qwen 3B (IQ 85-95)")
            Log.d("MyApplication", "  → P2P Mode: Qwen 3B")
            Log.d("MyApplication", "  ✨ Same model, different IQ levels for varied difficulty!")

        } catch (e: Exception) {
            Log.e("MyApplication", "❌ Error registering models", e)
            e.printStackTrace()
        }
    }
}
