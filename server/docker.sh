docker run \
  -it \
  --name iobc \
  --mount type=bind,source=/Users/simon/workspace/iobc,target=/iobc \
  --network setlnet \
  --volume setlnet \
  adoptopenjdk/openjdk11:alpine \
  /bin/sh
