import { HttpClientModule, provideHttpClient, withInterceptors } from '@angular/common/http'; // <-- Change this
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { BrowserModule, provideClientHydration, withEventReplay } from '@angular/platform-browser';
import { CodeInputModule } from 'angular-code-input';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { LoginComponent } from './pages/login/login.component';
import { RegisterComponent } from './pages/register/register.component';
// import { BookListComponent } from './modules/book/pages/book-list/book-list.component';
import { httpTokenInterceptor } from './services/interceptor/http-token.interceptor';
import { BookModule } from './modules/book/book.module';

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    RegisterComponent,

  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    HttpClientModule,
    CodeInputModule,
    BookModule
  ],
  providers: [
    provideClientHydration(withEventReplay()),
    // Remove HttpClient from providers,
    provideHttpClient(withInterceptors([httpTokenInterceptor]))
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }