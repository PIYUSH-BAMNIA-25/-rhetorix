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
            RunAnywhere.initialize(
                context = this@MyApplication,
                apiKey = "dev",  // Any string works in dev mode
                environment = SDKEnvironment.DEVELOPMENT
            )

            // Step 2: Register LLM Service Provider
            LlamaCppServiceProvider.register()

            // Step 3: Register Models
            registerModels()

            // Step 4: Scan for previously downloaded models
            RunAnywhere.scanForDownloadedModels()

            Log.i("MyApp", "SDK initialized successfully")

        } catch (e: Exception) {
            Log.e("MyApp", "SDK initialization failed: ${e.message}")
        }
    }

    private suspend fun registerModels() {
        try {
            // MODEL 1: BEGINNER AI (Llama 3.2 1B)
            // Downloads during signup (1-2 min download)
            // Used for: Beginner difficulty matches
            addModelFromURL(
                url = "https://huggingface.co/bartowski/Llama-3.2-1B-Instruct-GGUF/resolve/main/Llama-3.2-1B-Instruct-Q6_K_L.gguf",
                name = "Llama 3.2 1B Instruct Q6_K",
                type = "LLM"
            )
            Log.d("MyApplication", "📥 Registered Beginner Model: Llama 3.2 1B (815 MB)")

            // MODEL 2: ADVANCED AI (Qwen 2.5 3B)
            // Downloads in background during beginner matches
            // Used for: Intermediate, Advanced, and P2P modes
            addModelFromURL(
                url = "https://huggingface.co/Qwen/Qwen2.5-3B-Instruct-GGUF/resolve/main/qwen2.5-3b-instruct-q6_k.gguf",
                name = "Qwen 2.5 3B Instruct Q6_K",
                type = "LLM"
            )
            Log.d("MyApplication", "📥 Registered Advanced Model: Qwen 2.5 3B (2.3 GB)")

            Log.d("MyApplication", "🎮 Progressive Loading Strategy:")
            Log.d("MyApplication", "  → Signup: Download Llama 1B (815 MB)")
            Log.d("MyApplication", "  → Beginner Mode: Use Llama 1B")
            Log.d("MyApplication", "  → Background: Download Qwen 3B (2.3 GB)")
            Log.d("MyApplication", "  → Intermediate+: Use Qwen 3B")

        } catch (e: Exception) {
            Log.e("MyApplication", "❌ Error registering models", e)
        }
    }
}
