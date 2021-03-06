/*
 * Copyright (c) 2011-2015 EPFL DATA Laboratory
 * Copyright (c) 2014-2015 The Squall Collaboration (see NOTICE)
 *
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.epfl.data.squall.examples.imperative.shj;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ch.epfl.data.squall.components.DataSourceComponent;
import ch.epfl.data.squall.components.EquiJoinComponent;
import ch.epfl.data.squall.expressions.ColumnReference;
import ch.epfl.data.squall.expressions.IntegerYearFromDate;
import ch.epfl.data.squall.expressions.Multiplication;
import ch.epfl.data.squall.expressions.Subtraction;
import ch.epfl.data.squall.expressions.ValueExpression;
import ch.epfl.data.squall.expressions.ValueSpecification;
import ch.epfl.data.squall.operators.AggregateOperator;
import ch.epfl.data.squall.operators.AggregateSumOperator;
import ch.epfl.data.squall.operators.ProjectOperator;
import ch.epfl.data.squall.operators.SelectOperator;
import ch.epfl.data.squall.predicates.BetweenPredicate;
import ch.epfl.data.squall.predicates.ComparisonPredicate;
import ch.epfl.data.squall.query_plans.QueryBuilder;
import ch.epfl.data.squall.query_plans.QueryPlan;
import ch.epfl.data.squall.types.DateType;
import ch.epfl.data.squall.types.DoubleType;
import ch.epfl.data.squall.types.NumericType;
import ch.epfl.data.squall.types.StringType;
import ch.epfl.data.squall.types.Type;

public class TPCH8Plan extends QueryPlan {
    private static Logger LOG = Logger.getLogger(TPCH8Plan.class);

    private final QueryBuilder _queryBuilder = new QueryBuilder();

    private static final String _region = "AMERICA";
    private static final String _type = "ECONOMY ANODIZED STEEL";
    private static final String _date1Str = "1995-01-01";
    private static final String _date2Str = "1996-12-31";

    private static final Type<Date> _dateConv = new DateType();
    private static final NumericType<Double> _doubleConv = new DoubleType();
    private static final Type<String> _sc = new StringType();

    private static final Date _date1 = _dateConv.fromString(_date1Str);
    private static final Date _date2 = _dateConv.fromString(_date2Str);

    public TPCH8Plan(String dataPath, String extension, Map conf) {
	// -------------------------------------------------------------------------------------
	final List<Integer> hashRegion = Arrays.asList(0);

	final SelectOperator selectionRegion = new SelectOperator(
		new ComparisonPredicate(new ColumnReference(_sc, 1),
			new ValueSpecification(_sc, _region)));

	final ProjectOperator projectionRegion = new ProjectOperator(
		new int[] { 0 });

	final DataSourceComponent relationRegion = new DataSourceComponent(
		"REGION", dataPath + "region" + extension)
		.setOutputPartKey(hashRegion).add(selectionRegion)
		.add(projectionRegion);
	_queryBuilder.add(relationRegion);

	// -------------------------------------------------------------------------------------
	final List<Integer> hashNation1 = Arrays.asList(1);

	final ProjectOperator projectionNation1 = new ProjectOperator(
		new int[] { 0, 2 });

	final DataSourceComponent relationNation1 = new DataSourceComponent(
		"NATION1", dataPath + "nation" + extension).setOutputPartKey(
		hashNation1).add(projectionNation1);
	_queryBuilder.add(relationNation1);

	// -------------------------------------------------------------------------------------
	final EquiJoinComponent R_Njoin = new EquiJoinComponent(relationRegion,
		relationNation1).add(new ProjectOperator(new int[] { 1 }))
		.setOutputPartKey(Arrays.asList(0));
	_queryBuilder.add(R_Njoin);

	// -------------------------------------------------------------------------------------
	final List<Integer> hashCustomer = Arrays.asList(0);

	final ProjectOperator projectionCustomer = new ProjectOperator(
		new int[] { 3, 0 });

	final DataSourceComponent relationCustomer = new DataSourceComponent(
		"CUSTOMER", dataPath + "customer" + extension)
		.setOutputPartKey(hashCustomer).add(projectionCustomer);
	_queryBuilder.add(relationCustomer);

	// -------------------------------------------------------------------------------------
	final EquiJoinComponent R_N_Cjoin = new EquiJoinComponent(R_Njoin,
		relationCustomer).add(new ProjectOperator(new int[] { 1 }))
		.setOutputPartKey(Arrays.asList(0));
	_queryBuilder.add(R_N_Cjoin);

	// -------------------------------------------------------------------------------------
	final List<Integer> hashSupplier = Arrays.asList(1);

	final ProjectOperator projectionSupplier = new ProjectOperator(
		new int[] { 0, 3 });

	final DataSourceComponent relationSupplier = new DataSourceComponent(
		"SUPPLIER", dataPath + "supplier" + extension)
		.setOutputPartKey(hashSupplier).add(projectionSupplier);
	_queryBuilder.add(relationSupplier);

	// -------------------------------------------------------------------------------------
	final List<Integer> hashNation2 = Arrays.asList(0);

	final ProjectOperator projectionNation2 = new ProjectOperator(
		new int[] { 0, 1 });

	final DataSourceComponent relationNation2 = new DataSourceComponent(
		"NATION2", dataPath + "nation" + extension).setOutputPartKey(
		hashNation2).add(projectionNation2);
	_queryBuilder.add(relationNation2);

	// -------------------------------------------------------------------------------------
	final EquiJoinComponent S_Njoin = new EquiJoinComponent(
		relationSupplier, relationNation2).add(
		new ProjectOperator(new int[] { 0, 2 })).setOutputPartKey(
		Arrays.asList(0));
	_queryBuilder.add(S_Njoin);

	// -------------------------------------------------------------------------------------
	final List<Integer> hashPart = Arrays.asList(0);

	final SelectOperator selectionPart = new SelectOperator(
		new ComparisonPredicate(new ColumnReference(_sc, 4),
			new ValueSpecification(_sc, _type)));

	final ProjectOperator projectionPart = new ProjectOperator(
		new int[] { 0 });

	final DataSourceComponent relationPart = new DataSourceComponent(
		"PART", dataPath + "part" + extension)
		.setOutputPartKey(hashPart).add(selectionPart)
		.add(projectionPart);
	_queryBuilder.add(relationPart);

	// -------------------------------------------------------------------------------------
	final List<Integer> hashLineitem = Arrays.asList(1);

	// first field in projection
	final ColumnReference orderKey = new ColumnReference(_sc, 0);
	// second field in projection
	final ColumnReference partKey = new ColumnReference(_sc, 1);
	// third field in projection
	final ColumnReference suppKey = new ColumnReference(_sc, 2);
	// forth field in projection
	final ValueExpression<Double> substract = new Subtraction(
		new ValueSpecification(_doubleConv, 1.0), new ColumnReference(
			_doubleConv, 6));
	// extendedPrice*(1-discount)
	final ValueExpression<Double> product = new Multiplication(
		new ColumnReference(_doubleConv, 5), substract);
	final ProjectOperator projectionLineitem = new ProjectOperator(
		orderKey, partKey, suppKey, product);

	final DataSourceComponent relationLineitem = new DataSourceComponent(
		"LINEITEM", dataPath + "lineitem" + extension)
		.setOutputPartKey(hashLineitem).add(projectionLineitem);
	_queryBuilder.add(relationLineitem);

	// -------------------------------------------------------------------------------------
	final EquiJoinComponent P_Ljoin = new EquiJoinComponent(relationPart,
		relationLineitem).add(
		new ProjectOperator(new int[] { 1, 2, 3 })).setOutputPartKey(
		Arrays.asList(0));
	_queryBuilder.add(P_Ljoin);

	// -------------------------------------------------------------------------------------
	final List<Integer> hashOrders = Arrays.asList(0);

	final SelectOperator selectionOrders = new SelectOperator(
		new BetweenPredicate(new ColumnReference(_dateConv, 4), true,
			new ValueSpecification(_dateConv, _date1), true,
			new ValueSpecification(_dateConv, _date2)));

	// first field in projection
	final ValueExpression OrdersOrderKey = new ColumnReference(_sc, 0);
	// second field in projection
	final ValueExpression OrdersCustKey = new ColumnReference(_sc, 1);
	// third field in projection
	final ValueExpression OrdersExtractYear = new IntegerYearFromDate(
		new ColumnReference<Date>(_dateConv, 4));
	final ProjectOperator projectionOrders = new ProjectOperator(
		OrdersOrderKey, OrdersCustKey, OrdersExtractYear);

	final DataSourceComponent relationOrders = new DataSourceComponent(
		"ORDERS", dataPath + "orders" + extension)
		.setOutputPartKey(hashOrders).add(selectionOrders)
		.add(projectionOrders);
	_queryBuilder.add(relationOrders);

	// -------------------------------------------------------------------------------------
	final EquiJoinComponent P_L_Ojoin = new EquiJoinComponent(P_Ljoin,
		relationOrders).add(
		new ProjectOperator(new int[] { 1, 2, 3, 4 }))
		.setOutputPartKey(Arrays.asList(0));
	_queryBuilder.add(P_L_Ojoin);

	// -------------------------------------------------------------------------------------
	final EquiJoinComponent S_N_P_L_Ojoin = new EquiJoinComponent(S_Njoin,
		P_L_Ojoin).add(new ProjectOperator(new int[] { 1, 2, 3, 4 }))
		.setOutputPartKey(Arrays.asList(2));
	_queryBuilder.add(S_N_P_L_Ojoin);

	// -------------------------------------------------------------------------------------
	final AggregateOperator agg = new AggregateSumOperator(
		new ColumnReference(_doubleConv, 2), conf)
		.setGroupByColumns(Arrays.asList(1, 3));

	EquiJoinComponent finalComp = new EquiJoinComponent(R_N_Cjoin,
		S_N_P_L_Ojoin).add(agg);
	_queryBuilder.add(finalComp);

	// -------------------------------------------------------------------------------------

    }

    @Override
    public QueryBuilder getQueryPlan() {
	return _queryBuilder;
    }
}
