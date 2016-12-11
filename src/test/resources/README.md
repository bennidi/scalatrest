Define your test fixtures referenced in data.seed.* here

excludeAll(
          ExclusionRule(organization = "com.google.guava"),
          ExclusionRule(organization = "com.fasterxml.jackson.core"),
          ExclusionRule(organization = "org.apache.httpcomponents"),
          ExclusionRule(organization = "org.skyscreamer"),
          ExclusionRule(organization = "com.jayway.jsonpath"),
          ExclusionRule(organization = "net.sf.jopt-simple"))