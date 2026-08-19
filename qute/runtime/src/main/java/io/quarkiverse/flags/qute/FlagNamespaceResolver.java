package io.quarkiverse.flags.qute;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletionStage;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import io.quarkiverse.flags.Flag;
import io.quarkiverse.flags.Flag.Value;
import io.quarkiverse.flags.Flags;
import io.quarkus.qute.CompletedStage;
import io.quarkus.qute.EngineConfiguration;
import io.quarkus.qute.EvalContext;
import io.quarkus.qute.Expression;
import io.quarkus.qute.NamespaceResolver;
import io.quarkus.qute.Results;

@Singleton
@EngineConfiguration
public class FlagNamespaceResolver implements NamespaceResolver {

    public static final String NAMESPACE = "flag";

    @Inject
    Flags flags;

    @Override
    public CompletionStage<Object> resolve(EvalContext ctx) {
        String name = ctx.getName();
        if ("flags".equals(name)) {
            // flag:flags
            return cast(flags.findAll().subscribeAsCompletionStage());
        }
        // flag:bool('delta-feature')
        // flag:bool('delta-feature', true)
        // flag:enabled('delta-feature')
        // flag:on('delta-feature')
        // flag:disabled('delta-feature')
        // flag:off('delta-feature')
        // flag:string('delta-feature')
        // flag:int('delta-feature')
        // flag:int('delta-feature', 42)
        // flag:decimal('delta-feature')
        // flag:find('delta-feature')
        List<Expression> params = ctx.getParams();
        if (params.isEmpty()) {
            return Results.notFound(ctx);
        }
        return ctx.evaluate(params.get(0))
                .thenCompose(f -> {
                    if (params.size() > 1) {
                        return ctx.evaluate(params.get(1))
                                .thenCompose(defaultVal -> resolveFlag(ctx, f.toString(), defaultVal));
                    }
                    return resolveFlag(ctx, f.toString(), null);
                });
    }

    private CompletionStage<Object> resolveFlag(EvalContext ctx, String feature, Object defaultValue) {
        return flags.find(feature)
                .subscribeAsCompletionStage().thenCompose(flag -> {
                    if (flag.isEmpty()) {
                        return defaultValue != null ? CompletedStage.of(defaultValue) : Results.notFound(ctx);
                    }
                    if (defaultValue != null) {
                        return resolveWithDefault(ctx, flag.get(), defaultValue);
                    }
                    return switch (ctx.getName()) {
                        case "bool", "enabled", "on" -> cast(
                                flag.get().compute().map(Value::asBoolean).subscribeAsCompletionStage());
                        case "disabled", "off" -> cast(
                                flag.get().compute().map(v -> !v.asBoolean()).subscribeAsCompletionStage());
                        case "string" -> cast(
                                flag.get().compute().map(Value::asString).subscribeAsCompletionStage());
                        case "int" -> cast(
                                flag.get().compute().map(Value::asInt).subscribeAsCompletionStage());
                        case "decimal" -> cast(
                                flag.get().compute().map(Value::asDecimal).subscribeAsCompletionStage());
                        case "meta" -> CompletedStage.of(flag.get().metadata());
                        case "find" -> CompletedStage.of(flag.get());
                        default -> Results.notFound(ctx);
                    };
                });
    }

    private CompletionStage<Object> resolveWithDefault(EvalContext ctx, Flag flag, Object defaultValue) {
        return switch (ctx.getName()) {
            case "bool", "enabled", "on" -> {
                requireType(defaultValue, Boolean.class, ctx);
                yield cast(flag.compute().map(v -> v.asBoolean((Boolean) defaultValue))
                        .subscribeAsCompletionStage());
            }
            case "disabled", "off" -> {
                requireType(defaultValue, Boolean.class, ctx);
                boolean disabledDefault = (Boolean) defaultValue;
                yield cast(flag.compute().map(v -> {
                    try {
                        return !v.asBoolean();
                    } catch (java.util.NoSuchElementException e) {
                        return disabledDefault;
                    }
                }).subscribeAsCompletionStage());
            }
            case "string" -> {
                requireType(defaultValue, String.class, ctx);
                yield cast(flag.compute().map(v -> v.asString((String) defaultValue))
                        .subscribeAsCompletionStage());
            }
            case "int" -> {
                requireType(defaultValue, Integer.class, ctx);
                yield cast(flag.compute().map(v -> v.asInt((Integer) defaultValue))
                        .subscribeAsCompletionStage());
            }
            case "decimal" -> {
                requireType(defaultValue, BigDecimal.class, ctx);
                yield cast(flag.compute().map(v -> v.asDecimal((BigDecimal) defaultValue))
                        .subscribeAsCompletionStage());
            }
            case "meta" -> CompletedStage.of(flag.metadata());
            case "find" -> CompletedStage.of(flag);
            default -> Results.notFound(ctx);
        };
    }

    private static void requireType(Object value, Class<?> expectedType, EvalContext ctx) {
        if (!expectedType.isInstance(value)) {
            throw new IllegalArgumentException(
                    "Default value for flag:" + ctx.getName() + " must be " + expectedType.getSimpleName()
                            + " but got: " + value.getClass().getSimpleName());
        }
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @SuppressWarnings("unchecked")
    static <T> T cast(Object obj) {
        return (T) obj;
    }

}
