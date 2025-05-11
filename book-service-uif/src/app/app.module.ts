import { HttpClientModule, provideHttpClient, withInterceptors } from '@angular/common/http'; // <-- Change this
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { BrowserModule, provideClientHydration, withEventReplay } from '@angular/platform-browser';
import { CodeInputModule } from 'angular-code-input';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { BookCardComponent } from './modules/book/component/book-card/book-card.component';
import { MenuComponent } from './modules/book/component/menu/menu.component';
import { RattingComponent } from './modules/book/component/ratting/ratting.component';

import { LoginComponent } from './pages/login/login.component';
import { RegisterComponent } from './pages/register/register.component';
import { httpTokenInterceptor } from './services/interceptor/http-token.interceptor';
import { BookListComponent } from './modules/book/pages/book-list/book-list.component';
import { MainComponent } from './modules/book/pages/main/main.component';

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    RegisterComponent,
    MenuComponent,
    MainComponent,
    BookListComponent,
    BookCardComponent,
    RattingComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    HttpClientModule,
    CodeInputModule 
  ],
  providers: [
    provideClientHydration(withEventReplay()),
    // Remove HttpClient from providers,
    provideHttpClient(withInterceptors([httpTokenInterceptor]))
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }