# Keep generic signatures and runtime annotations used by library metadata/reflection.
-keepattributes Signature,*Annotation*,InnerClasses,EnclosingMethod

# GeckoView publishes its own consumer R8 rules. Avoid broad -keep rules here so
# application code can still be optimized.
