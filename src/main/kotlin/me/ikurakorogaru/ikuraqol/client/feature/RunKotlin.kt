package me.ikurakorogaru.ikuraqol.client.feature

import com.mojang.brigadier.arguments.StringArgumentType
import me.ikurakorogaru.ikuraqol.client.mainCommand
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.ClientCommands
import net.minecraft.network.chat.Component
import java.util.concurrent.CompletableFuture
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.host.StringScriptSource
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.api.ScriptDiagnostic
import net.minecraft.client.Minecraft
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors
import kotlin.script.experimental.api.ResultValue
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.jvm.BasicJvmScriptEvaluator
import kotlin.script.experimental.jvmhost.JvmScriptCompiler


object RunKotlin {
    private val compiler = JvmScriptCompiler()
    private val evaluator = BasicJvmScriptEvaluator()

    private val compileExecutor =
        Executors.newSingleThreadExecutor { task ->
            Thread(task, "IkuraQoL-Kotlin-Compiler").apply {
                isDaemon = true
            }
        }

    private val compilationConfiguration =
        ScriptCompilationConfiguration {
            jvm {
                dependenciesFromCurrentContext(
                    wholeClasspath = true
                )
            }
        }

    private val evaluationConfiguration =
        ScriptEvaluationConfiguration {}

    init {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(
                mainCommand.then(
                    ClientCommands.literal("runkotlin")
                        .then(
                            ClientCommands.argument("code", StringArgumentType.greedyString())
                                .executes { context ->val code = StringArgumentType.getString(context, "code")
                                    val source = context.source

                                    source.sendFeedback(
                                        Component.literal("Kotlinコードをコンパイル中...")
                                    )

                                    CompletableFuture.supplyAsync(
                                        {
                                            runBlocking {
                                                compiler(
                                                    StringScriptSource(code),
                                                    compilationConfiguration
                                                )
                                            }
                                        },
                                        compileExecutor
                                    ).thenAccept { compileResult ->
                                        Minecraft.getInstance().execute mainThread@{
                                            val compiledScript = when (compileResult) {
                                                is ResultWithDiagnostics.Success -> {
                                                    compileResult.value
                                                }

                                                is ResultWithDiagnostics.Failure -> {
                                                    val errors = compileResult.reports.filter {
                                                        it.severity == ScriptDiagnostic.Severity.ERROR ||
                                                                it.severity == ScriptDiagnostic.Severity.FATAL
                                                    }

                                                    if (errors.isEmpty()) {
                                                        source.sendFeedback(
                                                            Component.literal("コンパイルに失敗しました")
                                                        )
                                                    } else {
                                                        errors.forEach { report ->
                                                            source.sendFeedback(
                                                                Component.literal(
                                                                    "${report.severity}: ${report.message}"
                                                                )
                                                            )
                                                        }
                                                    }

                                                    return@mainThread
                                                }
                                            }

                                            // ここはMinecraftのメインスレッド
                                            val evaluationResult = runBlocking {
                                                evaluator(
                                                    compiledScript,
                                                    evaluationConfiguration
                                                )
                                            }

                                            val errors = evaluationResult.reports.filter {
                                                it.severity == ScriptDiagnostic.Severity.ERROR ||
                                                        it.severity == ScriptDiagnostic.Severity.FATAL
                                            }

                                            val runtimeError =
                                                if (evaluationResult is ResultWithDiagnostics.Success) {
                                                    evaluationResult.value.returnValue as? ResultValue.Error
                                                } else {
                                                    null
                                                }

                                            when {
                                                runtimeError != null -> {
                                                    source.sendFeedback(
                                                        Component.literal(
                                                            "実行エラー: ${runtimeError.error}"
                                                        )
                                                    )
                                                }

                                                errors.isNotEmpty() -> {
                                                    errors.forEach { report ->
                                                        source.sendFeedback(
                                                            Component.literal(
                                                                "${report.severity}: ${report.message}"
                                                            )
                                                        )
                                                    }
                                                }

                                                else -> {
                                                    source.sendFeedback(
                                                        Component.literal("コードを実行しました")
                                                    )
                                                }
                                            }
                                        }
                                    }.exceptionally { error ->
                                        Minecraft.getInstance().execute {
                                            source.sendFeedback(
                                                Component.literal(
                                                    "内部エラー: ${error.cause ?: error}"
                                                )
                                            )
                                        }

                                        null
                                    }

                                    1

                                }
                        )
                )
            )
        }
    }
}