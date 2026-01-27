package de.qaware.ekg.awb.da.elasticsearch.expression;

import de.qaware.ekg.awb.repository.api.dataobject.expr.*;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;

/**
 * A renderer/converter that will transform the EKG filter expression language
 * (object based API) to a similar representation ElasticSearch QueryBuilder
 * construct.
 */
public class ElasticSearchExpressionRenderer {

    /**
     * Renders the given {@link Expression} as a(nested) QueryBuilder to be used in a filter query
     * segment of an ElasticSearch BoolQueryBuilder. At the end this will result an 0-n filter queries
     * (not term queries) send to ElasticSearch.
     *
     * @param expression the {@link Expression}
     * @return the filter query as String
     */
    public QueryBuilder render(Expression expression) {
        Validate.notNull(expression);

        if (expression instanceof AndExpression) {
            return renderAndExpression((AndExpression) expression);

        } else if (expression instanceof ExactFilterExpression) {
            return renderExactFilterExpression((ExactFilterExpression) expression);

        } else if (expression instanceof NotExpression) {
            return renderNotExpression((NotExpression) expression);

        } else if (expression instanceof OrExpression) {
            return renderOrExpression((OrExpression) expression);

        } else if (expression instanceof RangeFilterExpression) {
            return renderRangeFilterExpression((RangeFilterExpression) expression);

        } else if (expression instanceof WildcardFilterExpression) {
            return renderWildcardFilterExpression((WildcardFilterExpression) expression);

        } else if (expression instanceof FullTextFilterExpression) {
            return renderFullTextFilterExpression((FullTextFilterExpression) expression);

        } else {
            throw new IllegalArgumentException(ElasticSearchExpressionRenderer.class.getSimpleName() + " does not support "
                    + expression.getClass().getSimpleName());
        }
    }

    private QueryBuilder renderAndExpression(AndExpression expression) {
        Validate.noNullElements(expression.getExpressions());

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        expression.getExpressions().stream().map(this::render).forEach(boolQueryBuilder::must);
        return boolQueryBuilder;
    }

    private QueryBuilder renderExactFilterExpression(ExactFilterExpression expression) {
        Validate.notNull(expression.getField());
        Validate.notNull(expression.getValues());

        return QueryBuilders.termsQuery(expression.getField().getName(), expression.getValues());
    }

    private QueryBuilder renderNotExpression(NotExpression expression) {
        Validate.notNull(expression.getExpression());

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.matchAllQuery());
        boolQueryBuilder.mustNot(render(expression.getExpression()));

        return boolQueryBuilder;
    }

    private QueryBuilder renderOrExpression(OrExpression expression) {
        Validate.noNullElements(expression.getExpressions());

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        expression.getExpressions().stream().map(this::render).forEach(boolQueryBuilder::should);
        return boolQueryBuilder;
    }

    private QueryBuilder renderRangeFilterExpression(RangeFilterExpression expression) {
        Validate.notNull(expression.getField());

        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(expression.getField().getName());

        if (StringUtils.isNotBlank(expression.getLowerBound())) {
            rangeQueryBuilder.from(expression.getLowerBound());
        }

        if (StringUtils.isNotBlank(expression.getUpperBound())) {
            rangeQueryBuilder.from(expression.getUpperBound());
        }

        return rangeQueryBuilder;
    }

    private QueryBuilder renderWildcardFilterExpression(WildcardFilterExpression expression) {
        Validate.notNull(expression.getField());
        Validate.notNull(expression.getValues());

        String field = expression.getField().getName();

        if (expression.getValues().size() > 1) {
            throw new NotImplementedException("The support for wildcard queries with multiple terms isn't implemented yet.");
        }

        if (expression.getValues().isEmpty()) {
            return QueryBuilders.matchAllQuery();
        }

        return QueryBuilders.wildcardQuery(field, expression.getValues().get(0));
    }

    private QueryBuilder renderFullTextFilterExpression(FullTextFilterExpression expression) {
        Validate.notNull(expression.getField());
        Validate.notEmpty(expression.getValue());

        return QueryBuilders.matchPhraseQuery(expression.getField().getName(), expression.getValue());
    }

}
