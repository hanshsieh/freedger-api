package org.freedger.di.hook;

import com.microsoft.azure.functions.spi.inject.FunctionInstanceInjector;

import org.freedger.di.component.DaggerAppComponent;
import org.freedger.controller.BackgroundWorker;
import org.freedger.controller.DittoApi;
import org.freedger.controller.LedgersApi;
import org.freedger.di.component.AppComponent;

/**
 * Injector for the Azure function classes to use Dagger injected instances. It's registered in
 * "src\main\resources\META-INF\services\com.microsoft.azure.functions.spi.inject.FunctionInstanceInjector"
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
    if (aClass == BackgroundWorker.class) {
      return (T) appComponent.buildBackgroundWorker();
    }
    throw new IllegalArgumentException("Unsupported class: " + aClass.getName());
  }
}
