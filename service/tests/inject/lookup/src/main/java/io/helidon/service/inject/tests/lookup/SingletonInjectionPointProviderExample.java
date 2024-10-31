/*
 * Copyright (c) 2024 Oracle and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.helidon.service.inject.tests.lookup;

import java.util.Optional;

import io.helidon.service.inject.api.Injection;
import io.helidon.service.inject.api.Injection.InjectionPointFactory;
import io.helidon.service.inject.api.Injection.QualifiedInstance;
import io.helidon.service.inject.api.Lookup;
import io.helidon.service.inject.api.Qualifier;

@Injection.Singleton
@SingletonInjectionPointProviderExample.FirstQuali
@SingletonInjectionPointProviderExample.SecondQuali
class SingletonInjectionPointProviderExample implements InjectionPointFactory<ContractSingleton> {
    static final Qualifier FIRST_QUALI = Qualifier.create(FirstQuali.class);
    static final Qualifier SECOND_QUALI = Qualifier.create(SecondQuali.class);
    static final QualifiedInstance<ContractSingleton> FIRST = QualifiedInstance.create(new FirstClass(), FIRST_QUALI);
    static final QualifiedInstance<ContractSingleton> SECOND = QualifiedInstance.create(new SecondClass(), SECOND_QUALI);

    @Override
    public Optional<QualifiedInstance<ContractSingleton>> first(Lookup lookup) {
        if (lookup.qualifiers().contains(FIRST_QUALI)) {
            return Optional.of(FIRST);
        }
        if (lookup.qualifiers().contains(SECOND_QUALI)) {
            return Optional.of(SECOND);
        }
        return Optional.empty();
    }

    @Injection.Qualifier
    @interface FirstQuali {
    }

    @Injection.Qualifier
    @interface SecondQuali {
    }

    static class FirstClass implements ContractSingleton {

    }

    static class SecondClass implements ContractSingleton {

    }
}
