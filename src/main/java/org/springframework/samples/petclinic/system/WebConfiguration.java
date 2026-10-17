package org.springframework.samples.petclinic.system;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.samples.petclinic.functional.web.EitherReturnValueHandler;
import com.dominikdorn.zazr.web.OptionPathVariableArgumentResolver;
import org.springframework.samples.petclinic.owner.StringToOwnerIdConverter;
import org.springframework.samples.petclinic.owner.StringToPetIdConverter;
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
	 * Registers the handler that lets controllers return {@code Either<DomainError, ?>},
	 * rendering business failures as values instead of thrown exceptions.
	 * @param handlers the list of custom return value handlers
	 */
	@Override
	public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> handlers) {
		handlers.add(new EitherReturnValueHandler());
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
	 * Prepends {@link OptionPathVariableArgumentResolver} ahead of Spring's built-in
	 * argument resolvers so that {@code @PathVariable Option<T>} parameters resolve to
	 * {@link dev.zazr.control.Option} (empty when absent) instead of {@code null}. The
	 * built-in {@code PathVariableMethodArgumentResolver} claims every
	 * {@code @PathVariable} parameter, so custom resolvers (which are consulted only
	 * afterwards) must be inserted at the front of the adapter's list.
	 * @return a post-processor that reorders the handler adapter's argument resolvers
	 */
	@Bean
	static BeanPostProcessor optionPathVariableResolverRegistrar() {
		return new BeanPostProcessor() {
			@Override
			public Object postProcessAfterInitialization(Object bean, String beanName) {
				if (bean instanceof RequestMappingHandlerAdapter adapter) {
					List<HandlerMethodArgumentResolver> existing = adapter.getArgumentResolvers();
					if (existing != null) {
						List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>();
						resolvers.add(new OptionPathVariableArgumentResolver());
						resolvers.addAll(existing);
						adapter.setArgumentResolvers(resolvers);
					}
				}
				return bean;
			}
		};
	}

}
