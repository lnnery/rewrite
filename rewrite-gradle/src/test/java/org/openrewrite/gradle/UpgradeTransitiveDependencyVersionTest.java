/*
 * Copyright 2024 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.gradle;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.gradle.Assertions.buildGradle;
import static org.openrewrite.gradle.toolingapi.Assertions.withToolingApi;

public class UpgradeTransitiveDependencyVersionTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
          .beforeRecipe(withToolingApi())
          .recipe(new UpgradeTransitiveDependencyVersion(
            "com.fasterxml*", "jackson-databind", "2.12.5", null, "CVE-2024-BAD"));
    }

    @Test
    void addConstraint() {
        rewriteRun(
          buildGradle(
            """
              plugins { id 'java' }
              repositories { mavenCentral() }
                            
              dependencies {
                  implementation 'org.openrewrite:rewrite-java:7.0.0'
              }
              """,
            """
              plugins { id 'java' }
              repositories { mavenCentral() }

              dependencies {
                  constraints {
                      implementation('com.fasterxml.jackson.core:jackson-databind:2.12.5') {
                          because 'CVE-2024-BAD'
                      }
                  }
                            
                  implementation 'org.openrewrite:rewrite-java:7.0.0'
              }
              """
          )
        );
    }

    @Test
    void addConstraintToExistingConstraintsBlock() {
        rewriteRun(
          buildGradle(
            """
              plugins { id 'java' }
              repositories { mavenCentral() }
                            
              dependencies {
                  implementation 'org.openrewrite:rewrite-java:7.0.0'
                  
                  constraints {
                  }
              }
              """,
            """
              plugins { id 'java' }
              repositories { mavenCentral() }

              dependencies {
                  implementation 'org.openrewrite:rewrite-java:7.0.0'
              
                  constraints {
                      implementation('com.fasterxml.jackson.core:jackson-databind:2.12.5') {
                          because 'CVE-2024-BAD'
                      }
                  }
              }
              """
          )
        );
    }
}
