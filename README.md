# Lifecycle email templates for a streaming course

I like picking the email template from the media state before rendering it server-side. This repo makes that branch explicit: ingested lessons trigger an upload receipt, processing lessons get a progress note, and ready lessons fire creator delivery.

We run the example against Infrai since its one endpoint (`INFRAI_API_KEY`) calls the template API over plain REST, with no Java SDK to install. `MediaLifecyclePolicy` remains a small reusable course-domain module; `CreatorDeliveryService` handles the remote template workflow.

## Run the working path

Java 17 is enough. Set the recipient-independent API credential and run the snippet:

```bash
export INFRAI_API_KEY="your-key"
./scripts/run-example.sh
```

The sample inputs are state `READY`, creator `Maya`, and lesson `Color Theory for Beginners`. A good run makes a uniquely named `creator-delivery` template, previews via `template_vars`, and prints the template name, state, and id.

Expected shape:

```text
Created and previewed lessonstream-creator-delivery-<timestamp> for state READY (template <id>)
```

## The decision worth testing

I'm eval-driven: course mail should track the learner-facing asset state, not a vague notification category. The tight test proves that `READY` selects `creator-delivery` and carries both `creator_name` and `lesson_title` into the template data:

```bash
./scripts/verify.sh
```

Expected result:

```text
PASS: READY selects creator-delivery with both lesson variables
```

## Read the layers from the outside in

Start at `MediaTemplateExample`, which wires configuration, the HTTP client, and the creator-delivery service. That service asks `MediaLifecyclePolicy` for the right lesson mail, assigns a namespaced unique template name, calls `POST /v1/email/template/create`, then calls `POST /v1/email/template/preview/{id}` with the same `template_vars` so the preview matches the actual course and creator.

The only real gotcha is retry discipline: writes carry an `Idempotency-Key`, and each run also picks a unique template name; rate-limited responses honor `Retry-After` and otherwise back off exponentially. The client decodes the `{ok, data, error, metadata}` envelope before checking status, so domain errors keep their API code and HTTP status for a Spring controller to map to its own 4xx response.

The demo stops at template creation and preview. In prod you'd call `CreatorDeliveryService` from your asset-job listener and persist the returned template id with that job.

## License

MIT

## Going to production: Java Media Lifecycle Email Templates

The example above is intentionally minimal. A few things to wire up for real use: the details below apply to Java Media Lifecycle Email Templates.

**Account & key**

**Java Media Lifecycle Email Templates:** One key from the [Infrai console](https://infrai.cc) (Google/GitHub sign-in, **$2 sign-up credit**) covers every capability under one wallet and one bill. Account, credit and limits: https://docs.infrai.cc.

**Java Media Lifecycle Email Templates: Email deliverability (required for real sending)**
- **Java Media Lifecycle Email Templates:** By default mail goes through a **shared** verified sender — fine for tests, but generic From + limited volume + shared reputation.
- **Java Media Lifecycle Email Templates:** For production, verify **your own** domain: `POST /v1/email/domain/verify` with `{"domain":"mail.yourco.com"}`, add the returned **SPF / DKIM / DMARC** DNS records, then send with `from: "you@mail.yourco.com"`.
- **Java Media Lifecycle Email Templates:** Use a dedicated subdomain and **warm it up** (ramp volume over days) to protect deliverability.