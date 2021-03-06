/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.spark.sql.catalyst

import java.math.BigInteger
import java.sql.Timestamp

import org.scalatest.FunSuite

import org.apache.spark.sql.catalyst.types._

case class PrimitiveData(
    intField: Int,
    longField: Long,
    doubleField: Double,
    floatField: Float,
    shortField: Short,
    byteField: Byte,
    booleanField: Boolean)

case class NullableData(
    intField: java.lang.Integer,
    longField: java.lang.Long,
    doubleField: java.lang.Double,
    floatField: java.lang.Float,
    shortField: java.lang.Short,
    byteField: java.lang.Byte,
    booleanField: java.lang.Boolean,
    stringField: String,
    decimalField: BigDecimal,
    timestampField: Timestamp,
    binaryField: Array[Byte])

case class OptionalData(
    intField: Option[Int],
    longField: Option[Long],
    doubleField: Option[Double],
    floatField: Option[Float],
    shortField: Option[Short],
    byteField: Option[Byte],
    booleanField: Option[Boolean],
    structField: Option[PrimitiveData])

case class ComplexData(
    arrayField: Seq[Int],
    arrayFieldContainsNull: Seq[java.lang.Integer],
    mapField: Map[Int, Long],
    mapFieldValueContainsNull: Map[Int, java.lang.Long],
    structField: PrimitiveData)

case class GenericData[A](
    genericField: A)

class ScalaReflectionSuite extends FunSuite {
  import ScalaReflection._

  test("primitive data") {
    val schema = schemaFor[PrimitiveData]
    assert(schema === Schema(
      StructType(Seq(
        StructField("intField", IntegerType, nullable = false),
        StructField("longField", LongType, nullable = false),
        StructField("doubleField", DoubleType, nullable = false),
        StructField("floatField", FloatType, nullable = false),
        StructField("shortField", ShortType, nullable = false),
        StructField("byteField", ByteType, nullable = false),
        StructField("booleanField", BooleanType, nullable = false))),
      nullable = true))
  }

  test("nullable data") {
    val schema = schemaFor[NullableData]
    assert(schema === Schema(
      StructType(Seq(
        StructField("intField", IntegerType, nullable = true),
        StructField("longField", LongType, nullable = true),
        StructField("doubleField", DoubleType, nullable = true),
        StructField("floatField", FloatType, nullable = true),
        StructField("shortField", ShortType, nullable = true),
        StructField("byteField", ByteType, nullable = true),
        StructField("booleanField", BooleanType, nullable = true),
        StructField("stringField", StringType, nullable = true),
        StructField("decimalField", DecimalType, nullable = true),
        StructField("timestampField", TimestampType, nullable = true),
        StructField("binaryField", BinaryType, nullable = true))),
      nullable = true))
  }

  test("optional data") {
    val schema = schemaFor[OptionalData]
    assert(schema === Schema(
      StructType(Seq(
        StructField("intField", IntegerType, nullable = true),
        StructField("longField", LongType, nullable = true),
        StructField("doubleField", DoubleType, nullable = true),
        StructField("floatField", FloatType, nullable = true),
        StructField("shortField", ShortType, nullable = true),
        StructField("byteField", ByteType, nullable = true),
        StructField("booleanField", BooleanType, nullable = true),
        StructField("structField", schemaFor[PrimitiveData].dataType, nullable = true))),
      nullable = true))
  }

  test("complex data") {
    val schema = schemaFor[ComplexData]
    assert(schema === Schema(
      StructType(Seq(
        StructField(
          "arrayField",
          ArrayType(IntegerType, containsNull = false),
          nullable = true),
        StructField(
          "arrayFieldContainsNull",
          ArrayType(IntegerType, containsNull = true),
          nullable = true),
        StructField(
          "mapField",
          MapType(IntegerType, LongType, valueContainsNull = false),
          nullable = true),
        StructField(
          "mapFieldValueContainsNull",
          MapType(IntegerType, LongType, valueContainsNull = true),
          nullable = true),
        StructField(
          "structField",
          StructType(Seq(
            StructField("intField", IntegerType, nullable = false),
            StructField("longField", LongType, nullable = false),
            StructField("doubleField", DoubleType, nullable = false),
            StructField("floatField", FloatType, nullable = false),
            StructField("shortField", ShortType, nullable = false),
            StructField("byteField", ByteType, nullable = false),
            StructField("booleanField", BooleanType, nullable = false))),
          nullable = true))),
      nullable = true))
  }

  test("generic data") {
    val schema = schemaFor[GenericData[Int]]
    assert(schema === Schema(
      StructType(Seq(
        StructField("genericField", IntegerType, nullable = false))),
      nullable = true))
  }

  test("tuple data") {
    val schema = schemaFor[(Int, String)]
    assert(schema === Schema(
      StructType(Seq(
        StructField("_1", IntegerType, nullable = false),
        StructField("_2", StringType, nullable = true))),
      nullable = true))
  }

  test("get data type of a value") {
    // BooleanType
    assert(BooleanType === typeOfObject(true))
    assert(BooleanType === typeOfObject(false))

    // BinaryType
    assert(BinaryType === typeOfObject("string".getBytes))

    // StringType
    assert(StringType === typeOfObject("string"))

    // ByteType
    assert(ByteType === typeOfObject(127.toByte))

    // ShortType
    assert(ShortType === typeOfObject(32767.toShort))

    // IntegerType
    assert(IntegerType === typeOfObject(2147483647))

    // LongType
    assert(LongType === typeOfObject(9223372036854775807L))

    // FloatType
    assert(FloatType === typeOfObject(3.4028235E38.toFloat))

    // DoubleType
    assert(DoubleType === typeOfObject(1.7976931348623157E308))

    // DecimalType
    assert(DecimalType === typeOfObject(BigDecimal("1.7976931348623157E318")))

    // TimestampType
    assert(TimestampType === typeOfObject(java.sql.Timestamp.valueOf("2014-07-25 10:26:00")))

    // NullType
    assert(NullType === typeOfObject(null))

    def typeOfObject1: PartialFunction[Any, DataType] = typeOfObject orElse {
      case value: java.math.BigInteger => DecimalType
      case value: java.math.BigDecimal => DecimalType
      case _ => StringType
    }

    assert(DecimalType === typeOfObject1(
      new BigInteger("92233720368547758070")))
    assert(DecimalType === typeOfObject1(
      new java.math.BigDecimal("1.7976931348623157E318")))
    assert(StringType === typeOfObject1(BigInt("92233720368547758070")))

    def typeOfObject2: PartialFunction[Any, DataType] = typeOfObject orElse {
      case value: java.math.BigInteger => DecimalType
    }

    intercept[MatchError](typeOfObject2(BigInt("92233720368547758070")))

    def typeOfObject3: PartialFunction[Any, DataType] = typeOfObject orElse {
      case c: Seq[_] => ArrayType(typeOfObject3(c.head))
    }

    assert(ArrayType(IntegerType) === typeOfObject3(Seq(1, 2, 3)))
    assert(ArrayType(ArrayType(IntegerType)) === typeOfObject3(Seq(Seq(1,2,3))))
  }

  test("convert PrimitiveData to catalyst") {
    val data = PrimitiveData(1, 1, 1, 1, 1, 1, true)
    val convertedData = Seq(1, 1.toLong, 1.toDouble, 1.toFloat, 1.toShort, 1.toByte, true)
    assert(convertToCatalyst(data) === convertedData)
  }

  test("convert Option[Product] to catalyst") {
    val primitiveData = PrimitiveData(1, 1, 1, 1, 1, 1, true)
    val data = OptionalData(Some(1), Some(1), Some(1), Some(1), Some(1), Some(1), Some(true), Some(primitiveData))
    val convertedData = Seq(1, 1.toLong, 1.toDouble, 1.toFloat, 1.toShort, 1.toByte, true, convertToCatalyst(primitiveData))
    assert(convertToCatalyst(data) === convertedData)
  }
}
