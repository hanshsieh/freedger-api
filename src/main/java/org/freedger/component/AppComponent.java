package org.freedger.component;

import dagger.Component;
import javax.inject.Singleton;
import org.freedger.function.DittoApi;
import org.freedger.function.LedgersApi;
import org.freedger.module.AppModule;

@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {
  DittoApi buildDittoApi();

  LedgersApi buildLedgersApi();
}
