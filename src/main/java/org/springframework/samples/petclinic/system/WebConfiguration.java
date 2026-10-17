package org.springframework.samples.petclinic.system;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import com.dominikdorn.zazr.web.EitherReturnValueHandler;
import com.dominikdorn.zazr.web.OptionPathVariableArgumentResolver;
import org.springframework.samples.petclinic.functional.web.DomainErrorLeftHandler;
import org.springframework.samples.petclinic.owner.web.StringToOwnerIdConverter;
import org.springframework.samples.petclinic.owner.web.StringToPetIdConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Configures internationalization (i18n) support for the application.
 *
 * <p>
 * Handles loading language-specific messages, tracking the user's language, and allowing
 * language changes via the URL parameter (e.g., <code>?lang=de</code>).
 * </p>
 *
 * @author Anuj Ashok Potdar
 */
@Configuration
@SuppressWarnings("unused")
public class WebConfiguration implements WebMvcConfigurer {

	/**
	 * Uses session storage to remember the user’s language setting across requests.
	 * Defaults to English if nothing is specified.
	 * @return session-based {@link LocaleResolver}
	 */
	@Bean
	public LocaleResolver localeResolver() {
		SessionLocaleResolver resolver = new SessionLocaleResolver();
		resolver.setDefaultLocale(Locale.ENGLISH);
		return resolver;
	}

	/**
	 * Allows the app to switch languages using a URL parameter like
	 * <code>?lang=es</code>.
	 * @return a {@link LocaleChangeInterceptor} that handles the change
	 */
	@Bean
	public LocaleChangeInterceptor localeChangeInterceptor() {
		LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
		interceptor.setParamName("lang");
		return interceptor;
	}

	/**
	 * Registers the locale change interceptor so it can run on each request.
	 * @param registry where interceptors are added
	 */
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(localeChangeInterceptor());
	}

	/**
	 * Registers converters that bind path variables to typed ids, so controllers can
	 * declare parameters such as {@code Option<OwnerId>} instead of a raw {@code int}.
	 * @param registry the formatter/converter registry
	 */
	@Override
	public void addFormatters(FormatterRegistry registry) {
		registry.addConverter(new StringToOwnerIdConverter());
		registry.addConverter(new StringToPetIdConverter());
	}

	/**
	 * Prepends the zazr handlers ahead of Spring's built-in ones on the handler adapter:
	 * <ul>
	 * <li>{@link OptionPathVariableArgumentResolver} so {@code @PathVariable Option<T>}
	 * resolves to {@link dev.zazr.control.Option} (empty when absent) rather than
	 * {@code null} — the built-in resolver claims every {@code @PathVariable}, so ours
	 * must come first.</li>
	 * <li>{@link EitherReturnValueHandler} so {@code Either} returns (including on
	 * {@code @ResponseBody}/JSON endpoints) are unwrapped before the built-in body
	 * processor would serialize the raw {@code Either}. It is given the adapter's own
	 * message converters so JSON bodies render exactly as elsewhere.</li>
	 * </ul>
	 * @return a post-processor that reorders the handler adapter's resolvers/handlers
	 */
	@Bean
	static BeanPostProcessor zazrWebHandlersRegistrar() {
		return new BeanPostProcessor() {
			@Override
			public Object postProcessAfterInitialization(Object bean, String beanName) {
				if (bean instanceof RequestMappingHandlerAdapter adapter) {
					List<HandlerMethodArgumentResolver> resolvers = adapter.getArgumentResolvers();
					if (resolvers != null) {
						List<HandlerMethodArgumentResolver> withOption = new ArrayList<>();
						withOption.add(new OptionPathVariableArgumentResolver());
						withOption.addAll(resolvers);
						adapter.setArgumentResolvers(withOption);
					}
					List<HandlerMethodReturnValueHandler> handlers = adapter.getReturnValueHandlers();
					if (handlers != null) {
						List<HandlerMethodReturnValueHandler> withEither = new ArrayList<>();
						withEither.add(new EitherReturnValueHandler(adapter.getMessageConverters(),
								new DomainErrorLeftHandler()));
						withEither.addAll(handlers);
						adapter.setReturnValueHandlers(withEither);
					}
				}
				return bean;
			}
		};
	}

}
