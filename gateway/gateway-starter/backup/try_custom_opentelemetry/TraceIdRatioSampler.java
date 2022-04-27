package com.wl4g.iam.gateway.trace;

/**
 * @see {@link io.opentelemetry.sdk.trace.samplers.AutoValue_TraceIdRatioBasedSampler}
 */
@Getter
public class TraceIdRatioSampler implements Sampler {
    private final double ratio;
    private final long idUpperBound;
    private final SamplingResult positiveSamplingResult;
    private final SamplingResult negativeSamplingResult;

    public static TraceIdRatioSampler create(double ratio) {
        long idUpperBound;
        if (ratio < 0.0D || ratio > 1.0D) {
            throw new IllegalArgumentException("ratio must be in range [0.0, 1.0]");
        }
        if (ratio == 0.0D) {
            idUpperBound = Long.MIN_VALUE;
        } else if (ratio == 1.0D) {
            idUpperBound = Long.MAX_VALUE;
        } else {
            idUpperBound = (long) (ratio * 9.223372036854776E18D);
        }
        return new TraceIdRatioSampler(ratio, idUpperBound, SamplingResult.create(SamplingDecision.RECORD_AND_SAMPLE),
                SamplingResult.create(SamplingDecision.DROP));
    }

    private TraceIdRatioSampler(double ratio, long idUpperBound, SamplingResult positiveSamplingResult,
            SamplingResult negativeSamplingResult) {
        this.ratio = ratio;
        this.idUpperBound = idUpperBound;
        if (positiveSamplingResult == null) {
            throw new NullPointerException("Null positiveSamplingResult");
        }
        this.positiveSamplingResult = positiveSamplingResult;
        if (negativeSamplingResult == null) {
            throw new NullPointerException("Null negativeSamplingResult");
        }
        this.negativeSamplingResult = negativeSamplingResult;
    }

    @Override
    public final SamplingResult shouldSample(
            Context parentContext,
            String traceId,
            String name,
            SpanKind spanKind,
            Attributes attributes,
            List<LinkData> parentLinks) {
        return (Math.abs(OtelEncodingUtils.longFromBase16String(traceId, 16)) < getIdUpperBound()) ? getPositiveSamplingResult()
                : getNegativeSamplingResult();
    }

    @Override
    public final String getDescription() {
        return String.format("TraceIdRatioBased{%.6f}", new Object[] { Double.valueOf(getRatio()) });
    }

    public final String toString() {
        return getDescription();
    }
}
