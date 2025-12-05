![CSPDog Logo](/docs/img/CSPDogLogo.png)

# CSPDog - Automatic CSP enforcement for Java servlet apps
Drop-in Java Servlet Filter that rewrites HTML responses, injects nonces/hashes, and produces CSP headers so your legacy Java apps can reach CSP compliance without massive frontend rewrites.

Get going immediately:
* Add the Maven dependency
* Enable the filter
* Set the CSP policy 
* Your app is CSP Compliant!

Profit! :)

# Why CSPDog?
* CSP is **powerful but hard to retrofit** into legacy Java apps (JSP, JSF, server-side templates).
* **Typical problems**: inline scripts/styles, on* attributes, third-party widgets, nonce management, and breaking UI.
* CSPDog **automates** nonce/hash generation and performs safe, **deterministic HTML rewriting** at the servlet layer so teams can enforce CSP without a risky, expensive frontend rewrite.

# High-level architecture
CSPDog library sits right before your web pages are sent to the web browser, rewriting them so they are comply with your CSP Policy:
![CSPDog HLA](/docs/img/CSPDog-HLA-Stroke-wider.png)

# Installation
## Add the Open Core dependency
### Maven
```aiexclude
<dependency>
  <groupId>com.softwaresicario</groupId>
  <artifactId>cspdog</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>
```
### Gradle
```aiexclude
implementation 'com.softwaresicario:cspdog:0.0.1-SNAPSHOT'
```
# Quickstart (Servlet)
## Register the servlet filter
### Web.xml configuration
```aiexclude
<filter>
  <filter-name>cspdog</filter-name>
  <filter-class>com.cspdog.filter.CspDogServletFilter</filter-class>
  <init-param>
    <param-name>mode</param-name>
    <param-value>enforce</param-value> <!-- audit | enforce -->
  </init-param>
</filter>

<filter-mapping>
  <filter-name>cspdog</filter-name>
  <url-pattern>/*</url-pattern>
</filter-mapping>
```
### Programmatic registration
```aiexclude
public class AppInitializer implements ServletContainerInitializer {
  @Override
  public void onStartup(Set<Class<?>> c, ServletContext ctx) {
    FilterRegistration.Dynamic reg = ctx.addFilter("cspdog", new CspDogServletFilter());
    reg.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/*");
    reg.setInitParameter("mode", "audit");
  }
}
```

# Modes of operation
* Audit — analyze responses and report what would break; do not enforce. Great for staged rollouts.
* Enforce — actively rewrite responses and emit strict CSP headers. Blocks non-compliant resources in the browser.
* Hybrid — enforce for selected domains or paths, audit elsewhere. Useful for incremental adoption.

# Features
* Body rewrite: introduce CSP transparently in your roadmap
* Per-request cryptographically-strong nonce generation and injection into `<script>` and `<style>` blocks (nonce)
* Hash computation for inline blocks and header insertion (unsafe-inline)
* Audit mode with report-only header and structured violation logs (reporting server)
* Pluggable HTML rewriter (performance)
* Graceful error handling (robustness)

# Roadmap (short)
* v0.1 (MVP) — servlet filter, nonce generation, basic rewriting, audit mode.
* v0.2 (Beta) — Spring Boot starter, JSP/Thymeleaf helpers, hash support.
* v0.3 — strict-dynamic support, worker-src, manifest-src, improved rewriter.
* v1.0 (Open Core Launch) — stable Open Core; docs, examples, CI artifacts.
* v1.1Enterprise (private) — dashboards, dashboards + monitoring, SLA-backed builds, performance parser.

# Development & Contributing
Contributions are explicitly welcome, read [CONTRIBUTING.md](CONTRIBUTING.md) for details on:
* Coding standards and tests
* How to run the project locally
* Branching & PR workflow
* Security disclosure policy

## Quick dev steps
```aiexclude
# clone
git clone https://github.com/your-org/cspdog.git
cd cspdog

# build
mvn clean install

# run example
cd examples/simple-servlet
mvn jetty:run
```
If you want to help but aren’t sure where to start, check the good first issue label in GitHub issues.

# License & Commercial
* **Open Core:** The code in /cspdog-core is released under AGPL v3 (see [LICENSE.md](LICENSE.md)).
* **Commercial:** A commercial license is available for organizations that cannot comply with AGPL or need enterprise features, SLAs, or private builds. See COMMERCIAL_LICENSE.md for a short summary and contact info.

If you are evaluating for a SaaS product or embedding in a closed-source product, please contact [sales](mailto:sales@cspdog.com) for licensing options.

# Support / Contact
* GitHub Issues — for bugs & feature requests (open-core)
* Discussions — design questions, proposals, community help
* For enterprise support / pilots / licensing: [sales@cspdog.com](mailto:sales@cspdog.com)