/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.tooling.internal.consumer

import org.gradle.tooling.events.test.TestOperationDescriptor
import org.gradle.tooling.internal.consumer.async.AsyncConsumerActionExecutor
import org.gradle.tooling.internal.consumer.connection.ConsumerAction
import org.gradle.tooling.internal.consumer.connection.ConsumerConnection
import org.gradle.tooling.internal.consumer.parameters.ConsumerOperationParameters
import org.gradle.tooling.internal.protocol.ResultHandlerVersion1
import spock.lang.Specification

class DefaultTestLauncherTest extends Specification {
    def executor = Mock(AsyncConsumerActionExecutor)
    def connection = Mock(ConsumerConnection)
    def launcher = new DefaultTestLauncher(executor, Stub(ConnectionParameters))

    def "adding tests does not affect an operation in progress"() {
        given:
        launcher.withJvmTestClasses("test")

        when:
        launcher.run()

        then:
        1 * executor.run(_, _) >> { ConsumerAction action, ResultHandlerVersion1 handler ->
            action.run(connection)
            handler.onComplete(null)
        }
        1 * connection.runTests(_, _) >> { TestExecutionRequest request, ConsumerOperationParameters params ->
            assert request.testClassNames == ["test"]
            assert request.testExecutionDescriptors == []

            launcher.withJvmTestClasses("test2")
            launcher.withTests(Stub(TestOperationDescriptor))

            assert request.testClassNames == ["test"]
            assert request.testExecutionDescriptors == []
        }
    }
}
