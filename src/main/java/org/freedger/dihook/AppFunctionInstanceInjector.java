package org.freedger.dihook;

import org.freedger.component.AppComponent;
import org.freedger.component.DaggerAppComponent;
import org.freedger.function.DittoApi;
import org.freedger.function.LedgersApi;

import com.microsoft.azure.functions.spi.inject.FunctionInstanceInjector;

/**
 * Injector for the Azure function classes to use Dagger injected instances.
 * It's registered in "src\main\resources\META-INF\services\com.microsoft.azure.functions.spi.inject.FunctionInstanceInjector"
 */
public class AppFunctionInstanceInjector implements FunctionInstanceInjector {
    private static final AppComponent appComponent = DaggerAppComponent.create();

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getInstance(Class<T> aClass) throws Exception {
        if (aClass == DittoApi.class) {
            return (T) appComponent.buildDittoApi();
        }
        if (aClass == LedgersApi.class) {
            return (T) appComponent.buildLedgersApi();
        }
        throw new IllegalArgumentException("Unsupported class: " + aClass.getName());
    }
}
