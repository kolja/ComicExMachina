(ns tools.helpers)

(defmacro for-indexed [[item coll] & body]
  `(for [i# (range (count ~coll))] 
     (let [~item [i# (nth ~coll i#)]]
       ~@body)))
