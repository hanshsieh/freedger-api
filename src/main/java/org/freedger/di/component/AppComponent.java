package org.freedger.di.component;

import dagger.Component;
import javax.inject.Singleton;

import org.freedger.controller.DittoApi;
import org.freedger.controller.LedgersApi;
import org.freedger.di.module.AppModule;

@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {
  DittoApi buildDittoApi();

  LedgersApi buildLedgersApi();
}
