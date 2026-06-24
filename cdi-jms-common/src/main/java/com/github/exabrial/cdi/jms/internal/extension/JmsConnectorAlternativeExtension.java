package com.github.exabrial.cdi.jms.internal.extension;

import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.Priority;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.enterprise.inject.spi.ProcessBeanAttributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.exabrial.cdi.jms.internal.api.JmsConnector;
import com.github.exabrial.cdi.jms.internal.api.JmsContextConnector;

public class JmsConnectorAlternativeExtension implements Extension {
	private static final Logger log = LoggerFactory.getLogger(JmsConnectorAlternativeExtension.class);

	private final Map<Class<?>, Class<?>> winners = new HashMap<>();

	<T extends JmsConnector> void observeJmsConnector(@Observes final ProcessAnnotatedType<T> pat) {
		recordCandidate(JmsConnector.class, pat.getAnnotatedType().getJavaClass());
	}

	<T extends JmsContextConnector> void observeJmsContextConnector(@Observes final ProcessAnnotatedType<T> pat) {
		recordCandidate(JmsContextConnector.class, pat.getAnnotatedType().getJavaClass());
	}

	void vetoLosers(@Observes final ProcessBeanAttributes<?> pba) {
		final Class<?> beanClass = pba.getAnnotated() instanceof jakarta.enterprise.inject.spi.AnnotatedType<?> annotatedType
				? annotatedType.getJavaClass()
				: null;
		if (beanClass == null) {
			return;
		}
		if (JmsConnector.class.isAssignableFrom(beanClass) && !beanClass.equals(winners.get(JmsConnector.class))) {
			log.info("vetoLosers() vetoing JmsConnector loser:{}", beanClass.getName());
			pba.veto();
		} else if (JmsContextConnector.class.isAssignableFrom(beanClass) && !beanClass.equals(winners.get(JmsContextConnector.class))) {
			log.info("vetoLosers() vetoing JmsContextConnector loser:{}", beanClass.getName());
			pba.veto();
		}
	}

	private void recordCandidate(final Class<?> connectorType, final Class<?> candidateClass) {
		log.info("recordCandidate() connectorType:{} candidate:{}", connectorType.getSimpleName(), candidateClass.getName());
		final boolean alternative = isAlternative(candidateClass);
		final boolean enabled = alternative && candidateClass.isAnnotationPresent(Priority.class);
		if (alternative && !enabled) {
			log.debug("recordCandidate() skipping disabled alternative (no @Priority):{}", candidateClass.getName());
		} else {
			final Class<?> currentWinner = winners.get(connectorType);
			if (currentWinner == null) {
				winners.put(connectorType, candidateClass);
			} else {
				final int currentPriority = getPriority(currentWinner);
				final int candidatePriority = getPriority(candidateClass);
				if (enabled && candidatePriority > currentPriority) {
					log.info("recordCandidate() new winner:{} (priority:{}) replaces:{} (priority:{})", candidateClass.getName(),
							candidatePriority, currentWinner.getName(), currentPriority);
					winners.put(connectorType, candidateClass);
				} else if (!isAlternative(currentWinner) && !alternative) {
					log.warn("recordCandidate() multiple non-alternative implementations for connectorType:{}", connectorType.getSimpleName());
				}
			}
		}
	}

	static final boolean isAlternative(final Class<?> clazz) {
		return clazz.isAnnotationPresent(Alternative.class);
	}

	static final int getPriority(final Class<?> clazz) {
		final Priority priority = clazz.getAnnotation(Priority.class);
		final int priorityValue;
		if (priority == null) {
			priorityValue = 0;
		} else {
			priorityValue = priority.value();
		}
		return priorityValue;
	}
}
