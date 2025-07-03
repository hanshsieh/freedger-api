package org.freedger.component;

import javax.inject.Singleton;

import org.freedger.DittoApi;
import org.freedger.module.AppModule;

import dagger.Component;

@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {
    DittoApi buildDittoApi();
}
