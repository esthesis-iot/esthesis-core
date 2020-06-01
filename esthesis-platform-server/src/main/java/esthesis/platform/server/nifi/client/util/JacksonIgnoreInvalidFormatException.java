package esthesis.platform.server.nifi.client.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import lombok.extern.java.Log;

import java.io.IOException;
import java.util.logging.Level;

/**
 * A utility handler for Jackson to ignore unexpected runtime errors during deserialization. This is
 * useful in situations where you need to process unstable payloads that you don't control. Be
 * cautious when using this handler. You should first try to understand the problematic payload and
 * mitigate any possible errors.
 */
@Log
public class JacksonIgnoreInvalidFormatException extends DeserializationProblemHandler {

  @Override
  public boolean handleUnknownProperty(DeserializationContext ctxt, JsonParser p,
    JsonDeserializer<?> deserializer, Object beanOrClass, String propertyName)
    throws IOException {
    log.log(Level.FINEST, "JSON deserialization: Ignoring unknown property {0}.", propertyName);
    return false;
  }

  @Override
  public Object handleWeirdKey(DeserializationContext ctxt, Class<?> rawKeyType, String keyValue,
    String failureMsg) {
    log.log(Level.FINEST, "JSON deserialization: Ignoring weird key {0}, {1}.",
      new Object[]{keyValue, failureMsg});
    return null;
  }

  @Override
  public Object handleWeirdStringValue(DeserializationContext ctxt, Class<?> targetType,
    String valueToConvert, String failureMsg) {
    log.log(Level.FINEST, "JSON deserialization: Ignoring weird string value {0}, {1}.",
      new Object[]{valueToConvert, failureMsg});
    return null;
  }

  @Override
  public Object handleWeirdNumberValue(DeserializationContext ctxt, Class<?> targetType,
    Number valueToConvert, String failureMsg) {
    log.log(Level.FINEST, "JSON deserialization: Ignoring weird number value {0}, {1}.",
      new Object[]{valueToConvert, failureMsg});
    return null;
  }

  @Override
  public Object handleWeirdNativeValue(DeserializationContext ctxt, JavaType targetType,
    Object valueToConvert, JsonParser p) {
    log.log(Level.FINEST, "JSON deserialization: Ignoring weird native value {0}.",
      valueToConvert);
    return null;
  }

  @Override
  public Object handleUnexpectedToken(DeserializationContext ctxt, Class<?> targetType, JsonToken t,
    JsonParser p, String failureMsg) {
    log.log(Level.FINEST, "JSON deserialization: Ignoring unexpected token {0}, {1}.",
      new Object[]{t.toString(), failureMsg});
    return null;
  }

  @Override
  public Object handleInstantiationProblem(DeserializationContext ctxt, Class<?> instClass,
    Object argument, Throwable t) {
    log.log(Level.FINEST, "JSON deserialization: Ignoring instantiation value {0}.", argument);
    return null;
  }

  @Override
  public Object handleMissingInstantiator(DeserializationContext ctxt, Class<?> instClass,
    ValueInstantiator valueInsta, JsonParser p, String msg) {
    log.log(Level.FINEST, "JSON deserialization: Ignoring missing instantiator {0}, {1}.",
      new Object[]{valueInsta.getValueClass(), msg});
    return null;
  }

  @Override
  public JavaType handleUnknownTypeId(DeserializationContext ctxt, JavaType baseType,
    String subTypeId, TypeIdResolver idResolver, String failureMsg) {
    log.log(Level.FINEST, "JSON deserialization: Ignoring unknown type Id {0}, {1}.",
      new Object[]{baseType.getTypeName(), failureMsg});
    return null;
  }

  @Override
  public JavaType handleMissingTypeId(DeserializationContext ctxt, JavaType baseType,
    TypeIdResolver idResolver, String failureMsg) {
    log.log(Level.FINEST, "JSON deserialization: Ignoring missing type Id {0}, {1}.",
      new Object[]{idResolver.idFromBaseType(), failureMsg});
    return null;
  }
}
