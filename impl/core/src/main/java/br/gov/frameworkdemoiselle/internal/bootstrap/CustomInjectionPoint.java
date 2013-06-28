/*
 * Demoiselle Framework
 * Copyright (C) 2010 SERPRO
 * ----------------------------------------------------------------------------
 * This file is part of Demoiselle Framework.
 * 
 * Demoiselle Framework is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License version 3
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License version 3
 * along with this program; if not,  see <http://www.gnu.org/licenses/>
 * or write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA  02110-1301, USA.
 * ----------------------------------------------------------------------------
 * Este arquivo é parte do Framework Demoiselle.
 * 
 * O Framework Demoiselle é um software livre; você pode redistribuí-lo e/ou
 * modificá-lo dentro dos termos da GNU LGPL versão 3 como publicada pela Fundação
 * do Software Livre (FSF).
 * 
 * Este programa é distribuído na esperança que possa ser útil, mas SEM NENHUMA
 * GARANTIA; sem uma garantia implícita de ADEQUAÇÃO a qualquer MERCADO ou
 * APLICAÇÃO EM PARTICULAR. Veja a Licença Pública Geral GNU/LGPL em português
 * para maiores detalhes.
 * 
 * Você deve ter recebido uma cópia da GNU LGPL versão 3, sob o título
 * "LICENCA.txt", junto com esse programa. Se não, acesse <http://www.gnu.org/licenses/>
 * ou escreva para a Fundação do Software Livre (FSF) Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02111-1301, USA.
 */
package br.gov.frameworkdemoiselle.internal.bootstrap;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;

public class CustomInjectionPoint implements InjectionPoint {

	private final Bean<?> bean;

	private final Type beanType;

	private final Set<Annotation> qualifiers;

	public CustomInjectionPoint(Bean<?> bean, Type beanType, Annotation... qualifiers) {
		this.bean = bean;
		this.beanType = beanType;
		this.qualifiers = new HashSet<Annotation>(Arrays.asList(qualifiers));
	}

	@Override
	public Type getType() {
		return this.beanType;
	}

	@Override
	public Set<Annotation> getQualifiers() {
		return this.qualifiers;
	}

	@Override
	public Bean<?> getBean() {
		return this.bean;
	}

	@Override
	public Member getMember() {
		return null;
	}

	@Override
	public boolean isDelegate() {
		return false;
	}

	@Override
	public boolean isTransient() {
		return false;
	}

	@Override
	public Annotated getAnnotated() {
		return new Annotated() {

			@Override
			public Type getBaseType() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Set<Type> getTypeClosure() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			@SuppressWarnings("unchecked")
			public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
				T result = null;

				for (Annotation annotation : getAnnotations()) {
					if (annotation.annotationType() == annotationType) {
						result = (T) annotation;
						break;
					}
				}

				return result;
			}

			@Override
			public Set<Annotation> getAnnotations() {
				return qualifiers;
			}

			@Override
			public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
				return qualifiers.contains(annotationType);
			}
		};
	}
}